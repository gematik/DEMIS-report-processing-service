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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_XML;

import ca.uhn.fhir.context.FhirContext;
import feign.Response;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidationServiceRelaxedModeTest {

  private static final String ORIGINAL_NOTIFICATION =
      """
<Bundle xmlns="http://hl7.org/fhir">
    <id value="098f6bcd-4621-3373-8ade-4e832627b4f6" />
    <id value="9aaaaaaa-4621-3373-8ade-bbbbbbbbbbbb" />
</Bundle>
""";

  private static final String CORRECTED_NOTIFICATION =
      """
{"resourceType":"Bundle","id":"098f6bcd-4621-3373-8ade-4e832627b4f6"}""";

  private static final FhirContext fhirContext = FhirContext.forR4Cached();

  @Mock ValidationServiceClient validationServiceClient;

  private ValidationServiceConnectionService underTest;

  private static Response mockResponse(final int status, final String content) throws IOException {
    final Response response = Mockito.mock(Response.class);
    Mockito.when(response.status()).thenReturn(status);
    if (content != null) {
      final Response.Body body = Mockito.mock(Response.Body.class);
      Mockito.when(body.asReader(StandardCharsets.UTF_8)).thenReturn(new StringReader(content));
      Mockito.when(response.body()).thenReturn(body);
    }
    return response;
  }

  private static OperationOutcome createOperationOutcomeOfValidationService() {
    final OperationOutcome outcome = new OperationOutcome();
    outcome.addIssue().setSeverity(IssueSeverity.FATAL);
    outcome.addIssue().setSeverity(IssueSeverity.ERROR);
    outcome.addIssue().setSeverity(IssueSeverity.INFORMATION);
    return outcome;
  }

  private static String fhirResourceToJson(final IBaseResource theResource) {
    return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(theResource);
  }

  @BeforeEach
  void setup() {
    underTest = new ValidationServiceConnectionService(validationServiceClient, fhirContext);
    underTest.relaxedMode = true;
    underTest.outcomeIssueThreshold = IssueSeverity.INFORMATION;
  }

  @Test
  void parsedFhirNotificationIsValid() throws Exception {
    final var outcome = createOperationOutcomeOfValidationService();
    final var firstResponse = mockResponse(422, fhirResourceToJson(outcome));
    Mockito.when(validationServiceClient.validateBundleXml(ORIGINAL_NOTIFICATION))
        .thenReturn(firstResponse);

    final var secondTryResponse = mockResponse(200, null);
    Mockito.when(validationServiceClient.validateBundleJson(CORRECTED_NOTIFICATION))
        .thenReturn(secondTryResponse);

    final ValidationResult result =
        underTest.validateBundle(APPLICATION_XML, ORIGINAL_NOTIFICATION);
    assertThat(result).isNotNull().returns(true, ValidationResult::isValid);
    assertThat(result.operationOutcome()).isNotNull();
    assertThat(result.operationOutcome().getIssue())
        .extracting(OperationOutcomeIssueComponent::getSeverity)
        .containsExactly(IssueSeverity.WARNING, IssueSeverity.WARNING, IssueSeverity.INFORMATION);
  }

  @Test
  void parsedFhirNotificationIsStillInvalid() throws Exception {
    final var outcome = createOperationOutcomeOfValidationService();
    final var firstResponse = mockResponse(422, fhirResourceToJson(outcome));
    Mockito.when(validationServiceClient.validateBundleXml(ORIGINAL_NOTIFICATION))
        .thenReturn(firstResponse);

    final var secondTryResponse = mockResponse(422, null);
    Mockito.when(validationServiceClient.validateBundleJson(CORRECTED_NOTIFICATION))
        .thenReturn(secondTryResponse);

    final ValidationResult result =
        underTest.validateBundle(APPLICATION_XML, ORIGINAL_NOTIFICATION);

    assertThat(result).isNotNull().returns(false, ValidationResult::isValid);

    assertThat(result.operationOutcome()).isNotNull();
    assertThat(result.operationOutcome().getIssue())
        .extracting(OperationOutcomeIssueComponent::getSeverity)
        .containsExactly(IssueSeverity.FATAL, IssueSeverity.ERROR, IssueSeverity.INFORMATION);
  }

  @Test
  void fhirNotificationNotParseable() throws Exception {
    final String notParseableNotification =
        """
<Bundle xmlns="http://hl7.org/fhir">
    <id value="098f6bcd-4621-3373-8ade-4e832627b4f6" />
    <syntax error
</Bundle>
""";
    final var outcome = createOperationOutcomeOfValidationService();
    final var firstResponse = mockResponse(422, fhirResourceToJson(outcome));
    Mockito.when(validationServiceClient.validateBundleXml(notParseableNotification))
        .thenReturn(firstResponse);
    // Note: No second try (that's the difference to parsedFhirNotificationIsStillInvalid)

    final ValidationResult result =
        underTest.validateBundle(APPLICATION_XML, notParseableNotification);

    assertThat(result).isNotNull().returns(false, ValidationResult::isValid);

    assertThat(result.operationOutcome()).isNotNull();
    assertThat(result.operationOutcome().getIssue())
        .extracting(OperationOutcomeIssueComponent::getSeverity)
        .containsExactly(IssueSeverity.FATAL, IssueSeverity.ERROR, IssueSeverity.INFORMATION);
  }
}
