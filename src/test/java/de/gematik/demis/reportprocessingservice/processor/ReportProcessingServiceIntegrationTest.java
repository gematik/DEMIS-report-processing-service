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

import static de.gematik.demis.reportprocessingservice.utils.Constants.ALL_OK;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_ATOM_XML;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.NotifierDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.ReportBedOccupancyDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.ReportBundleDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.StatisticInformationBedOccupancyDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.technicals.AddressDataBuilder;
import de.gematik.demis.reportprocessingservice.connectors.hls.HospitalLocationServiceClient;
import de.gematik.demis.reportprocessingservice.connectors.ncapi.NotificationClearingApiClient;
import de.gematik.demis.reportprocessingservice.connectors.pdf.PdfGenerationConnectionService;
import de.gematik.demis.reportprocessingservice.connectors.validation.ValidationServiceClient;
import de.gematik.demis.reportprocessingservice.exceptions.RpsServiceException;
import de.gematik.demis.reportprocessingservice.internal.HospitalLocationDTO;
import de.gematik.demis.reportprocessingservice.internal.OperationOutcomeUtils;
import de.gematik.demis.reportprocessingservice.utils.UUID5Generator;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@EnableAutoConfiguration(exclude = {SpringDocConfiguration.class})
class ReportProcessingServiceIntegrationTest {

  private static final String REQUEST_ID = "request-id";
  private static final String NOTIFICATION_BUNDLE_ID =
      UUID5Generator.generateType5UUID(REQUEST_ID).toString();
  private static final String IK_NUMBER = "987654321";
  private static final String AZP = "demis-test";
  @MockBean ValidationServiceClient validationServiceClient;
  @MockBean NotificationClearingApiClient notificationClearingApiClient;
  @MockBean PdfGenerationConnectionService pdfGenerationConnectionService;
  @MockBean HospitalLocationServiceClient hospitalLocationServiceClient;
  @Autowired private ReportProcessingService reportProcessingService;
  @Autowired private FhirContext fhirContext;

  private static Response createResponse(final int status, final String content) {
    return Response.builder()
        .status(status)
        .body(content, StandardCharsets.UTF_8)
        .request(Mockito.mock(Request.class))
        .build();
  }

  @BeforeEach
  void init() {
    final OperationOutcome validationOutcome = new OperationOutcome();
    validationOutcome
        .addIssue()
        .setSeverity(IssueSeverity.WARNING)
        .setCode(IssueType.PROCESSING)
        .setDiagnostics("For Test");
    final String validationOutcomeJson =
        fhirContext.newJsonParser().encodeResourceToString(validationOutcome);
    when(validationServiceClient.validateBundleXml(anyString()))
        .thenReturn(createResponse(200, validationOutcomeJson));
    when(validationServiceClient.validateBundleJson(anyString()))
        .thenReturn(createResponse(200, validationOutcomeJson));
    when(notificationClearingApiClient.sendNotificationToNotificationClearingAPI(
            anyString(), anyString()))
        .thenReturn(ResponseEntity.ok().build());
    Binary binary = new Binary();
    binary.setData("Hello World".getBytes());
    when(pdfGenerationConnectionService.generateBedOccupancyReceipt(
            argThat(new NotificationIdMatcher(NOTIFICATION_BUNDLE_ID)), anyString()))
        .thenReturn(Optional.of(binary));
    when(hospitalLocationServiceClient.getHospitalData(anyString()))
        .thenReturn(
            ResponseEntity.ok()
                .body(
                    singletonList(
                        HospitalLocationDTO.builder()
                            .locationId("987654")
                            .label("Testkrankenhaus - gematik GmbH")
                            .houseNumber("136")
                            .line("Friedrichstr.")
                            .postalCode("10117")
                            .city("Berlin")
                            .ik("987654321")
                            .build())));
  }

  @ParameterizedTest
  @MethodSource("de.gematik.demis.reportprocessingservice.RequestDataProvider#okCombinations")
  void givenValidBodyAndValidMediaTypeWhenProcessThenNoErrors(
      String body, MediaType contentType, MediaType accept) {

    String result =
        reportProcessingService
            .process(body, contentType, REQUEST_ID, accept, IK_NUMBER, AZP)
            .getBody();

    if (accept.isWildcardType()) {
      accept = contentType;
    }
    IParser parser = fhirContext.newJsonParser();
    if (accept.equals(APPLICATION_XML)) {
      parser = fhirContext.newXmlParser();
    }
    final Parameters parameters = parser.parseResource(Parameters.class, result);
    assertThat(parameters.getParameter()).isNotEmpty().hasSize(2);

    final List<OperationOutcomeIssueComponent> issues =
        parameters.getParameter().stream()
            .map(Parameters.ParametersParameterComponent::getResource)
            .filter(Objects::nonNull)
            .filter(OperationOutcome.class::isInstance)
            .map(OperationOutcome.class::cast)
            .map(OperationOutcome::getIssue)
            .flatMap(Collection::parallelStream)
            .toList();

    assertThat(issues)
        .isNotEmpty()
        .extracting(
            OperationOutcomeIssueComponent::getSeverity, OperationOutcomeIssueComponent::getCode)
        .containsExactly(
            tuple(IssueSeverity.INFORMATION, IssueType.INFORMATIONAL),
            tuple(IssueSeverity.WARNING, IssueType.PROCESSING));

    final List<Binary> pdfBinary =
        parameters.getParameter().stream()
            .map(Parameters.ParametersParameterComponent::getResource)
            .filter(Objects::nonNull)
            .filter(Bundle.class::isInstance)
            .map(Bundle.class::cast)
            .map(Bundle::getEntry)
            .flatMap(Collection::stream)
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(Binary.class::isInstance)
            .map(Binary.class::cast)
            .toList();

    assertThat(pdfBinary).isNotEmpty().hasSize(1).first().hasFieldOrProperty("data");

    assertThat(issues.getFirst().getDetails().getText()).isEqualTo(ALL_OK);
  }

  @Test
  void shouldReturnOperationOutcomeForValidationError() {

    OperationOutcome operationOutcome =
        OperationOutcomeUtils.exceptionOperationOutcome("mock for validation failed");

    String operationOutcomeAsJson =
        fhirContext.newJsonParser().encodeResourceToString(operationOutcome);
    when(validationServiceClient.validateBundleJson(anyString()))
        .thenReturn(createResponse(422, operationOutcomeAsJson));

    String body =
        "{\"resourceType\":\"Bundle\",\"id\":\"1\", \"meta\": {\"profile\": [\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]}}";

    ResponseEntity<String> result =
        reportProcessingService.process(
            body, APPLICATION_JSON, REQUEST_ID, APPLICATION_JSON, IK_NUMBER, AZP);

    assertThat(result.getStatusCode().is4xxClientError()).isTrue();
    assertThat(result.getBody()).contains(operationOutcomeAsJson);
  }

  @Test
  void givenBlankAcceptWhenProcessThenUsingContentType() {

    String content =
        "{\"resourceType\":\"Bundle\",\"id\":\"24a55a68-7121-4a89-9178-8f7a1e639aec\",\"meta\":{\"lastUpdated\":\"2022-09-05T19:05:21.964+02:00\",\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\",\"value\":\"5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7\"},\"type\":\"document\",\"timestamp\":\"2022-09-05T19:05:21.964+02:00\",\"entry\":[{\"fullUrl\":\"https://demis.rki.de/fhir/Composition/5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationId\",\"value\":\"4afd102f-abf1-4ddc-980e-a06080034d61\"},\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"80563-0\",\"display\":\"Report\"}]},\"category\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportCategory\",\"code\":\"bedOccupancyReport\",\"display\":\"Bettenbelegungsstatistik\"}]}],\"subject\":{\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\",\"value\":\"987654\"}},\"date\":\"2022-09-05T19:05:21+02:00\",\"author\":[{\"reference\":\"PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\"}],\"title\":\"Bericht (Krankenhausbettenbelegungsstatistik)\",\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportSection\",\"code\":\"statisticInformationBedOccupancySection\",\"display\":\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"}]},\"entry\":[{\"reference\":\"QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\"}]}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"27911e87-6ce0-4833-85e3-b6fe1279412e\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"]},\"type\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/organizationType\",\"code\":\"hospital\",\"display\":\"Krankenhaus\"}]}],\"name\":\"Testkrankenhaus - gematik GmbH\",\"telecom\":[{\"system\":\"phone\",\"value\":\"0123456789\",\"use\":\"work\"}],\"address\":[{\"line\":[\"Friedrichstr. 136\"],\"city\":\"Berlin\",\"postalCode\":\"10117\"}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\",\"resource\":{\"resourceType\":\"PractitionerRole\",\"id\":\"5a47db03-a48f-4b5e-a638-9a59bb754841\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"]},\"organization\":{\"reference\":\"Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\"}}},{\"fullUrl\":\"https://demis.rki.de/fhir/QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\",\"resource\":{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"36a25277-c394-44a3-9027-f030282343a7\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"]},\"questionnaire\":\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\",\"status\":\"completed\",\"item\":[{\"linkId\":\"numberOperableBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":250}]},{\"linkId\":\"numberOccupiedBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":221}]},{\"linkId\":\"numberOperableBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":50}]},{\"linkId\":\"numberOccupiedBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":37}]}]}}]}";

    final String result =
        reportProcessingService
            .process(content, APPLICATION_JSON, REQUEST_ID, null, IK_NUMBER, AZP)
            .getBody();

    final Parameters parameters =
        fhirContext.newJsonParser().parseResource(Parameters.class, result);

    assertThat(parameters.getParameter()).isNotEmpty().hasSize(2);
  }

  @Test
  void givenBlankContentWhenProcessThenError() {

    assertThatThrownBy(
            () ->
                reportProcessingService.process(
                    "", APPLICATION_ATOM_XML, REQUEST_ID, APPLICATION_JSON, IK_NUMBER, AZP))
        .isInstanceOf(RpsServiceException.class)
        .hasMessageContaining("bundle profile not supported or missing(pre-check).");
  }

  @Test
  void givenContentNotMatchContentTypeWhenProcessThenError() {

    String content =
        """
                    {
                      "resourceType": "Bundle",
                      "meta": {
                        "profile": [
                          "https://demis.rki.de/fhir/StructureDefinition/ReportBundle"
                        ]
                      },
                      "entry": [
                        {
                          "resource": {
                            "resourceType": "Composition",
                            "identifier": {
                              "value": "composition-identifier"
                            }
                          }
                        }
                      ]
                    }""";
    assertThatThrownBy(
            () ->
                reportProcessingService.process(
                    content, APPLICATION_XML, REQUEST_ID, APPLICATION_JSON, IK_NUMBER, AZP))
        .isInstanceOf(DataFormatException.class)
        .hasMessageContaining("Unexpected character '{' (code 123) in prolog; expected '<'");
  }

  @Test
  void givenUnsupportedResourceBundleWhenProcessThenError() {

    String noBundle =
        """
                    {
                      "resourceType": "Patient",
                      "meta": {
                        "profile": [
                          "https://demis.rki.de/fhir/StructureDefinition/ReportBundle"
                        ]
                      },
                      "entry": [
                        {
                          "resource": {
                            "resourceType": "Composition",
                            "identifier": {
                              "value": "composition-identifier"
                            }
                          }
                        }
                      ]
                    }""";

    assertThatThrownBy(
            () ->
                reportProcessingService.process(
                    noBundle, APPLICATION_XML, REQUEST_ID, APPLICATION_JSON, IK_NUMBER, AZP))
        .isInstanceOf(DataFormatException.class)
        .hasMessageContaining("Unexpected character '{' (code 123) in prolog;");
  }

  @Test
  void givenUnsupportedProfileTypeWhenProcessThenError() {

    String content =
        "<Bundle><id value=\"1\"/>"
            + "<meta>"
            + "<profile value=\"https://demis.rki.de/fhir/StructureDefinition/Unsupported\"/>"
            + "</meta>"
            + "</Bundle>";

    assertThatThrownBy(
            () ->
                reportProcessingService.process(
                    content, APPLICATION_XML, REQUEST_ID, APPLICATION_JSON, IK_NUMBER, AZP))
        .isInstanceOf(RpsServiceException.class)
        .hasMessage("bundle profile not supported or missing(pre-check).");
  }

  @Test
  void
      shouldCreateReceiptBundleWithReceivedBundleIdentifierAsExtensionAndChecktIdentifierIsOverwritten() {

    Address address =
        new AddressDataBuilder()
            .setPostalCode("10117")
            .setCity("Berlin")
            .setStreet("Friedrichstr.")
            .setHouseNumber("136")
            .build();
    PractitionerRole practitioner =
        new NotifierDataBuilder()
            .setNotifierAddress(address)
            .setNotifierFacilityName("Testkrankenhaus - gematik GmbH")
            .buildReportExampleNotifierData();
    QuestionnaireResponse statisticData =
        new StatisticInformationBedOccupancyDataBuilder()
            .buildExampleStatisticInformationBedOccupancy();
    Composition composition =
        new ReportBedOccupancyDataBuilder()
            .setSubjectValue("987654")
            .buildExampleReportBedOccupancy(practitioner, statisticData);
    Bundle bundle =
        new ReportBundleDataBuilder()
            .setReportBedOccupancy(composition)
            .setIdentifierValue("5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7")
            .setNotifierRole(practitioner)
            .buildExampleReportBundle();
    String content = fhirContext.newJsonParser().encodeResourceToString(bundle);

    ResponseEntity<String> responseEntity =
        reportProcessingService.process(
            content, APPLICATION_JSON, null, APPLICATION_JSON, IK_NUMBER, AZP);

    List<String> expectedToBeContained =
        List.of("\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\"");

    List<String> expectedNotToBeContained =
        List.of("\"value\":\"5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7\"");

    assertThat(responseEntity.getBody()).contains(expectedToBeContained);
    assertThat(responseEntity.getBody()).doesNotContain(expectedNotToBeContained);
  }

  public static class NotificationIdMatcher implements ArgumentMatcher<Bundle> {

    private final String expectedNotificationId;

    public NotificationIdMatcher(String expectedNotificationId) {
      this.expectedNotificationId = expectedNotificationId;
    }

    @Override
    public boolean matches(Bundle bundle) {
      return bundle.getIdentifier() != null
          && expectedNotificationId.equals(bundle.getIdentifier().getValue());
    }
  }
}
