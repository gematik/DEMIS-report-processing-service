/*
 * Copyright [2024], gematik GmbH
 *
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
 */

package de.gematik.demis.reportprocessingservice.connectors.validation;

import static de.gematik.demis.reportprocessingservice.internal.OperationOutcomeUtils.filterOutcomeIssues;
import static de.gematik.demis.reportprocessingservice.internal.OperationOutcomeUtils.orderOutcomeIssues;
import static de.gematik.demis.reportprocessingservice.internal.OperationOutcomeUtils.reduceIssuesSeverityToWarn;
import static de.gematik.demis.reportprocessingservice.utils.ErrorCode.ERROR_IN_VALIDATION_CALL;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.demis.fhirparserlibrary.MessageType;
import de.gematik.demis.reportprocessingservice.decoder.RPSErrorDecoder;
import de.gematik.demis.service.base.error.ServiceCallException;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.StringDecoder;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationServiceConnectionService {

  private final ValidationServiceClient validationServiceClient;
  private final FhirContext fhirContext;

  private final Decoder stringDecoder = new StringDecoder();
  private final RPSErrorDecoder errorDecoder =
      new RPSErrorDecoder("validation-service", ERROR_IN_VALIDATION_CALL, ERROR_IN_VALIDATION_CALL);

  @Value("${rps.flags.relaxed-validation:false}")
  boolean relaxedMode;

  @Value("${rps.outcome-issue-threshold}")
  IssueSeverity outcomeIssueThreshold = IssueSeverity.WARNING;

  private static boolean isStatusSuccessful(final int status) {
    return status >= 200 && status < 300;
  }

  @PostConstruct
  void logConfig() {
    log.info("relaxedMode={}, outcomeIssueThreshold={}", relaxedMode, outcomeIssueThreshold);
  }

  public ValidationResult validateBundle(final MediaType contentType, final String content) {
    final var messageType = MessageType.getMessageType(contentType.getSubtype());

    final int status;
    final String body;
    try (final Response response = callValidationService(content, messageType)) {
      status = response.status();

      if (status != HttpStatus.UNPROCESSABLE_ENTITY.value() && !isStatusSuccessful(status)) {
        throw errorDecoder.decode("validate", response);
      }

      body = readResponse(response);
    }

    final OperationOutcome operationOutcome =
        body.isBlank()
            ? new OperationOutcome()
            : fhirContext.newJsonParser().parseResource(OperationOutcome.class, body);

    final boolean isValid;
    if (isStatusSuccessful(status)) {
      log.debug("Fhir Bundle successfully validated.");
      isValid = true;
    } else if (relaxedMode && (isValidInRelaxedMode(content, messageType))) {
      log.info(
          "Fhir report only valid with relaxed validation. Original validation output: {}",
          fhirResourceToJson(operationOutcome));
      reduceIssuesSeverityToWarn(operationOutcome);
      isValid = true;
    } else {
      isValid = false;
    }

    filterOutcomeIssues(operationOutcome, outcomeIssueThreshold);
    orderOutcomeIssues(operationOutcome);

    return new ValidationResult(isValid, operationOutcome);
  }

  private boolean isValidInRelaxedMode(
      final String fhirNotification, final MessageType contentType) {
    final String parsedNotificationAsJson;
    try {
      final IBaseResource bundle = getParser(contentType).parseResource(fhirNotification);
      parsedNotificationAsJson = fhirResourceToJson(bundle);
    } catch (final RuntimeException e) {
      log.debug("Notification is not parseable", e);
      return false;
    }

    try (final Response response =
        callValidationService(parsedNotificationAsJson, MessageType.JSON)) {
      return isStatusSuccessful(response.status());
    }
  }

  private String readResponse(final Response response) {
    try {
      return (String) stringDecoder.decode(response, String.class);
    } catch (final IOException e) {
      throw new ServiceCallException("error reading response", "VS", response.status(), e);
    }
  }

  private Response callValidationService(
      final String fhirNotification, final MessageType contentType) {
    return switch (contentType) {
      case JSON -> validationServiceClient.validateBundleJson(fhirNotification);
      case XML -> validationServiceClient.validateBundleXml(fhirNotification);
    };
  }

  private IParser getParser(final MessageType contentType) {
    return switch (contentType) {
      case JSON -> fhirContext.newJsonParser();
      case XML -> fhirContext.newXmlParser();
    };
  }

  private String fhirResourceToJson(final IBaseResource bundle) {
    final IParser fhirJsonParser = fhirContext.newJsonParser();
    return fhirJsonParser.encodeResourceToString(bundle);
  }
}
