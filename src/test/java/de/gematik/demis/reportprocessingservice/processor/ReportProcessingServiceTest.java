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

package de.gematik.demis.reportprocessingservice.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.fhirparserlibrary.ParsingException;
import de.gematik.demis.reportprocessingservice.connectors.ncapi.NotificationClearingApiConnectionService;
import de.gematik.demis.reportprocessingservice.connectors.pdf.PdfGenerationConnectionService;
import de.gematik.demis.reportprocessingservice.connectors.validation.ValidationResult;
import de.gematik.demis.reportprocessingservice.connectors.validation.ValidationServiceConnectionService;
import de.gematik.demis.reportprocessingservice.internal.HospitalLocationDataValidatorService;
import de.gematik.demis.reportprocessingservice.internal.ReportEnrichmentService;
import de.gematik.demis.reportprocessingservice.objects.TestObjects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ReportProcessingServiceTest {

  private static final String IK_NUMBER = "987654321";
  private static final String AZP = "demis-test";
  private ReportProcessingService reportProcessingService;

  @Mock private ValidationServiceConnectionService validationServiceConnectionServiceMock;

  @Mock
  private NotificationClearingApiConnectionService notificationClearingApiConnectionServiceMock;

  @Mock private HospitalLocationDataValidatorService hospitalLocationDataValidatorServiceMock;

  @Mock private ReportEnrichmentService reportEnrichmentServiceMock;

  @Mock private FhirParser fhirParserService;

  @Mock private PdfGenerationConnectionService pdfGenerationConnectionService;

  FhirContext fhirContext = FhirContext.forR4Cached();

  @BeforeEach
  void setUp() {
    reportProcessingService =
        new ReportProcessingService(
            validationServiceConnectionServiceMock,
            notificationClearingApiConnectionServiceMock,
            hospitalLocationDataValidatorServiceMock,
            reportEnrichmentServiceMock,
            fhirParserService,
            pdfGenerationConnectionService);
    reportProcessingService.init();
  }

  @Test
  void shouldCallParsingServiceForParseAndEncode() throws ParsingException {
    Bundle bundle = TestObjects.bundles().minimalBundle();
    String content = fhirContext.newJsonParser().encodeResourceToString(bundle);

    setupBundleIsValid();
    when(fhirParserService.parseBundleOrParameter(content, APPLICATION_JSON.getSubtype()))
        .thenReturn(bundle);
    when(fhirParserService.encode(any(), eq(APPLICATION_XML.getSubtype()))).thenReturn("foobar2");

    String process =
        reportProcessingService
            .process(content, APPLICATION_JSON, "request-id", APPLICATION_XML, IK_NUMBER, AZP)
            .getBody();

    assertThat(process).isEqualTo("foobar2");
  }

  @Test
  void shouldCallValidationServiceConnectionService() {
    Bundle bundle = TestObjects.bundles().minimalBundle();
    String content = fhirContext.newJsonParser().encodeResourceToString(bundle);

    setupBundleIsValid();
    when(fhirParserService.parseBundleOrParameter(content, APPLICATION_JSON.getSubtype()))
        .thenReturn(bundle);
    when(fhirParserService.encode(any(), eq(APPLICATION_XML.getSubtype()))).thenReturn("foobar2");

    String process =
        reportProcessingService
            .process(content, APPLICATION_JSON, "request-id", APPLICATION_XML, IK_NUMBER, AZP)
            .getBody();

    assertThat(process).isEqualTo("foobar2");

    verify(validationServiceConnectionServiceMock).validateBundle(APPLICATION_JSON, content);
  }

  @Test
  void shouldReturnResponseEntityWithNotOk() {
    Bundle bundle = TestObjects.bundles().minimalBundle();
    String content = fhirContext.newJsonParser().encodeResourceToString(bundle);

    OperationOutcome operationOutcome = new OperationOutcome();
    operationOutcome.setId("someId");
    when(validationServiceConnectionServiceMock.validateBundle(any(), any()))
        .thenReturn(new ValidationResult(false, operationOutcome));
    when(fhirParserService.encode(operationOutcome, APPLICATION_XML.getSubtype()))
        .thenReturn("foobar2");

    ResponseEntity<String> process =
        reportProcessingService.process(
            content, APPLICATION_JSON, "request-id", APPLICATION_XML, IK_NUMBER, AZP);

    assertThat(process.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(process.getBody()).isEqualTo("foobar2");
  }

  @Test
  void shouldCallEnrichmentService() throws ParsingException {
    Bundle bundle = TestObjects.bundles().minimalBundle();
    String content = fhirContext.newJsonParser().encodeResourceToString(bundle);

    setupBundleIsValid();
    when(fhirParserService.parseBundleOrParameter(content, APPLICATION_JSON.getSubtype()))
        .thenReturn(bundle);
    when(fhirParserService.encode(any(), eq(APPLICATION_XML.getSubtype()))).thenReturn("foobar2");

    reportProcessingService.process(
        content, APPLICATION_JSON, "request-id", APPLICATION_XML, IK_NUMBER, AZP);

    verify(reportEnrichmentServiceMock).enrichReportBundle(bundle, "request-id");
  }

  @Test
  void shouldAddAllAvailableTokenValues() throws ParsingException {
    reportProcessingService =
        new ReportProcessingService(
            validationServiceConnectionServiceMock,
            notificationClearingApiConnectionServiceMock,
            hospitalLocationDataValidatorServiceMock,
            reportEnrichmentServiceMock,
            fhirParserService,
            pdfGenerationConnectionService);
    reportProcessingService.init();

    Bundle bundle = TestObjects.bundles().minimalBundle();
    String content = fhirContext.newJsonParser().encodeResourceToString(bundle);

    setupBundleIsValid();
    when(fhirParserService.parseBundleOrParameter(content, APPLICATION_JSON.getSubtype()))
        .thenReturn(bundle);
    when(fhirParserService.encode(any(), eq(APPLICATION_XML.getSubtype()))).thenReturn("foobar2");

    reportProcessingService.process(
        content, APPLICATION_JSON, "request-id", APPLICATION_XML, IK_NUMBER, AZP);

    verify(reportEnrichmentServiceMock).enrichReportBundle(bundle, "request-id");
  }

  private void setupBundleIsValid() {
    when(validationServiceConnectionServiceMock.validateBundle(any(), any()))
        .thenReturn(new ValidationResult(true, new OperationOutcome()));
  }
}
