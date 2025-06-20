package de.gematik.demis.reportprocessingservice.processor;

/*-
 * #%L
 * report-processing-service
 * %%
 * Copyright (C) 2025 gematik GmbH
 * %%
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static de.gematik.demis.reportprocessingservice.internal.OperationOutcomeUtils.producePositiveOutcomeWithNoErrors;
import static de.gematik.demis.reportprocessingservice.utils.ErrorCode.PROFILE_NOT_SUPPORTED;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.fhirparserlibrary.ParsingException;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.receipt.ReceiptBuilder;
import de.gematik.demis.reportprocessingservice.connectors.ces.ContextEnrichmentService;
import de.gematik.demis.reportprocessingservice.connectors.ncapi.NotificationClearingApiConnectionService;
import de.gematik.demis.reportprocessingservice.connectors.pdf.PdfGenerationConnectionService;
import de.gematik.demis.reportprocessingservice.connectors.validation.ValidationResult;
import de.gematik.demis.reportprocessingservice.connectors.validation.ValidationServiceConnectionService;
import de.gematik.demis.reportprocessingservice.exceptions.RpsServiceException;
import de.gematik.demis.reportprocessingservice.internal.HospitalLocationDataValidatorService;
import de.gematik.demis.reportprocessingservice.internal.ReportEnrichmentService;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportProcessingService {

  public static final String HTTPS_DEMIS_RKI_DE_FHIR_STRUCTURE_DEFINITION_REPORT_BUNDLE =
      "https://demis.rki.de/fhir/StructureDefinition/ReportBundle";

  private final ValidationServiceConnectionService validationServiceConnectionService;
  private final NotificationClearingApiConnectionService notificationClearingApiConnectionService;
  private final HospitalLocationDataValidatorService hospitalLocationDataValidatorService;
  private final ReportEnrichmentService reportEnrichmentService;
  private final FhirParser fhirParserService;
  private final PdfGenerationConnectionService pdfGenerationConnectionService;
  private final ContextEnrichmentService contextEnrichmentService;

  private Pattern pattern;

  @PostConstruct
  void init() {
    pattern =
        Pattern.compile(
            "(?s)meta.*profile.*"
                + Pattern.quote(HTTPS_DEMIS_RKI_DE_FHIR_STRUCTURE_DEFINITION_REPORT_BUNDLE)
                + "(?!\\w|/)");
  }

  @Observed(
      name = "process-report",
      contextualName = "process-report",
      lowCardinalityKeyValues = {"notification", "fhir"})
  public ResponseEntity<String> process(
      String content,
      MediaType contentType,
      String requestId,
      MediaType accept,
      String ikNumber,
      String azp,
      String authorization)
      throws ParsingException {

    preCheckProfile(content);
    contentType = MediaType.parseMediaType(contentType.getType() + "/" + contentType.getSubtype());
    accept = handleAcceptHeader(contentType, accept);

    // 1. Validation extern
    final ValidationResult validationResult =
        validationServiceConnectionService.validateBundle(contentType, content);
    if (!validationResult.isValid()) {
      return ResponseEntity.unprocessableEntity()
          .body(fhirParserService.encode(validationResult.operationOutcome(), accept.getSubtype()));
    }

    Bundle bundle = fhirParserService.parseBundleOrParameter(content, contentType.getSubtype());
    log.info(
        "received report from hospital with IK number {}. processing bundle {} with content type {}",
        ikNumber,
        bundle.getId(),
        contentType.getSubtype());

    // 2. Hospital Location Validation extern
    hospitalLocationDataValidatorService.validateLocationData(ikNumber, bundle);

    // 3. Bundle Enrichment (Empfänger, Zeitdaten aktualisieren) intern
    reportEnrichmentService.enrichReportBundle(bundle, requestId);

    // 4. Bundle Enrichment (Context Enrichment Service) intern
    contextEnrichmentService.enrichBundleWithContextInformation(bundle, authorization);

    // 5. PDF generieren extern
    Optional<Binary> pdf =
        pdfGenerationConnectionService.generateBedOccupancyReceipt(bundle, requestId);

    // 6. NCAPI extern
    notificationClearingApiConnectionService.sendReportBundleToNCAPI(bundle);

    Parameters returnParameters =
        createReturnValue(bundle, pdf, validationResult.operationOutcome());

    String result = fhirParserService.encode(returnParameters, accept.getSubtype());
    log.info("bundleId={}, sender={}, status=ok", bundle.getId(), azp);
    log.debug("response bundle: {}", result);
    return ResponseEntity.ok().contentType(accept).body(result);
  }

  private void preCheckProfile(final String fhirNotification) {
    Matcher matcher = pattern.matcher(fhirNotification);
    if (!matcher.find()) {
      throw new RpsServiceException(
          PROFILE_NOT_SUPPORTED, "bundle profile not supported or missing(pre-check).");
    }
  }

  private MediaType handleAcceptHeader(MediaType contentType, MediaType accept) {
    if (accept == null || accept.toString().contains("*")) {
      return contentType;
    }
    return accept;
  }

  private Parameters createReturnValue(
      final Bundle bundle,
      final Optional<Binary> pdfBinary,
      final OperationOutcome validationOutcome) {
    Parameters returnParameters = new Parameters();

    ReceiptBuilder receiptBuilder =
        new ReceiptBuilder()
            .setNotificationBundleId(bundle.getIdentifier().getValue())
            .setRelatesToId(
                ((Composition) bundle.getEntryFirstRep().getResource()).getIdentifier().getValue());

    pdfBinary.ifPresent(receiptBuilder::setPdfQuittung);

    final OperationOutcome operationOutcome = producePositiveOutcomeWithNoErrors();
    validationOutcome.getIssue().forEach(operationOutcome::addIssue);

    Bundle receiptBundle = receiptBuilder.createReportReceiptBundle();

    returnParameters.addParameter().setName("bundle").setResource(receiptBundle);
    returnParameters.addParameter().setName("operationOutcome").setResource(operationOutcome);

    return returnParameters;
  }
}
