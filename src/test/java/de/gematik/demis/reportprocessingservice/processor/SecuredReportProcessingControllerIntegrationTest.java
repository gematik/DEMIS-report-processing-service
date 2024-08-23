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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static de.gematik.demis.reportprocessingservice.utils.Constants.ALL_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.ReportBedOccupancyDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.ReportBundleDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Utils;
import de.gematik.demis.reportprocessingservice.IDPFaker;
import de.gematik.demis.reportprocessingservice.properties.OAuth2Properties;
import de.gematik.demis.reportprocessingservice.testobjects.TestObjects;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc()
@SpringBootTest
@AutoConfigureWireMock(port = 0) // dynamic random port
@TestPropertySource(locations = "classpath:application-test.properties")
class SecuredReportProcessingControllerIntegrationTest {

  private static final String IK_NUMBER_HEADER = "IK-Number";
  private static final String IK_NUMBER = "987654321";
  private static final String AZP_HEADER = "azp";
  private static final String AZP = "demis-test";
  private static final WireMockServer validationServer = new WireMockServer(7070);
  private static final WireMockServer clearingAPIServer = new WireMockServer(7071);
  private static final WireMockServer hlsServer = new WireMockServer(7072);
  private static final WireMockServer pdfServer = new WireMockServer(7073);
  private static final IDPFaker idpFaker = new IDPFaker();
  private static final String VS_ALL_OKAY_JSON = "{\"resourceType\":\"OperationOutcome\"}";
  public static final String HOSPITAL_ID = "772557";
  @Autowired private OAuth2Properties oAuth2Properties;
  @Autowired private ObjectMapper jsonMapper;
  @Autowired private FhirContext fhirContext;

  @Value("${wiremock-issuer-uri}")
  private String issuerUri;

  @Autowired private MockMvc mockMvc;

  @BeforeAll
  public static void startServers() {
    validationServer.start();
    clearingAPIServer.start();
    hlsServer.start();
    pdfServer.start();
  }

  @AfterAll
  public static void stopServers() {
    validationServer.stop();
    clearingAPIServer.stop();
    hlsServer.stop();
    pdfServer.stop();
  }

  @BeforeEach
  public void init() {
    idpFaker.fakingServer(issuerUri);
  }

  @ParameterizedTest
  @MethodSource("de.gematik.demis.reportprocessingservice.RequestDataProvider#okCombinations")
  void givenValidRequestWhenPostReportThen200(String body, MediaType contentType, MediaType accept)
      throws Exception {
    if (accept.isWildcardType()) {
      accept = contentType;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setAccept(List.of(accept));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(200).withBody(VS_ALL_OKAY_JSON)));

    configureFor(hlsServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathMatching(".*\\/hospital-locations.*"))
            .willReturn(
                okJson(
                    """
                        [
                            {
                                "id": 987654,
                                "ik": 987654321,
                                "label": "Testkrankenhaus - gematik GmbH",
                                "postalCode": 10117,
                                "city": "Berlin",
                                "line": "Friedrichstr.",
                                "houseNumber": "136"
                            }
                        ]""")));

    configureFor(pdfServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

    configureFor(clearingAPIServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
            .willReturn(aResponse().withStatus(200)));

    mockMvc
        .perform(post("/$process-report").headers(headers).content(body))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", containsString(accept.toString())))
        // .andExpect(content().string(containsString(randomAlphabetic))) //bundle id
        .andExpect(content().string(containsString("information"))) // severity
        .andExpect(content().string(containsString(ALL_OK)))
        .andExpect(content().string(containsString("TW9ja1BkZg==")));
  }

  @ParameterizedTest
  @MethodSource("de.gematik.demis.reportprocessingservice.RequestDataProvider#okCombinations")
  void lgivenValidRequestWhenPostReportThen200WithoutAcceptHeader(
      String body, MediaType contentType, MediaType accept) throws Exception {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(200).withBody(VS_ALL_OKAY_JSON)));

    configureFor(hlsServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathMatching(".*\\/hospital-locations.*"))
            .willReturn(
                okJson(
                    """
                        [
                            {
                                "id": 987654,
                                "ik": 987654321,
                                "label": "Testkrankenhaus - gematik GmbH",
                                "postalCode": 10117,
                                "city": "Berlin",
                                "line": "Friedrichstr.",
                                "houseNumber": "136"
                            }
                        ]""")));

    configureFor(pdfServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

    configureFor(clearingAPIServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
            .willReturn(aResponse().withStatus(200)));

    mockMvc
        .perform(post("/$process-report").headers(headers).content(body))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", containsString(contentType.toString())))
        .andExpect(content().string(containsString("information"))) // severity
        .andExpect(content().string(containsString(ALL_OK)))
        .andExpect(content().string(containsString("TW9ja1BkZg==")));
  }

  @Test
  void shouldReturnReceiptWithoutPDFBinaryWhenErrorFromPDF() throws Exception {
    String body =
        "{\"resourceType\":\"Bundle\",\"id\":\"24a55a68-7121-4a89-9178-8f7a1e639aec\",\"meta\":{\"lastUpdated\":\"2022-09-05T19:05:21.964+02:00\",\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\",\"value\":\"5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7\"},\"type\":\"document\",\"timestamp\":\"2022-09-05T19:05:21.964+02:00\",\"entry\":[{\"fullUrl\":\"https://demis.rki.de/fhir/Composition/5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationId\",\"value\":\"4afd102f-abf1-4ddc-980e-a06080034d61\"},\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"80563-0\",\"display\":\"Report\"}]},\"category\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportCategory\",\"code\":\"bedOccupancyReport\",\"display\":\"Bettenbelegungsstatistik\"}]}],\"subject\":{\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\",\"value\":\"987654\"}},\"date\":\"2022-09-05T19:05:21+02:00\",\"author\":[{\"reference\":\"PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\"}],\"title\":\"Bericht (Krankenhausbettenbelegungsstatistik)\",\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportSection\",\"code\":\"statisticInformationBedOccupancySection\",\"display\":\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"}]},\"entry\":[{\"reference\":\"QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\"}]}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"27911e87-6ce0-4833-85e3-b6fe1279412e\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"]},\"type\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/organizationType\",\"code\":\"hospital\",\"display\":\"Krankenhaus\"}]}],\"name\":\"Testkrankenhaus - gematik GmbH\",\"telecom\":[{\"system\":\"phone\",\"value\":\"0123456789\",\"use\":\"work\"}],\"address\":[{\"line\":[\"Friedrichstr. 136\"],\"city\":\"Berlin\",\"postalCode\":\"10117\"}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\",\"resource\":{\"resourceType\":\"PractitionerRole\",\"id\":\"5a47db03-a48f-4b5e-a638-9a59bb754841\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"]},\"organization\":{\"reference\":\"Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\"}}},{\"fullUrl\":\"https://demis.rki.de/fhir/QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\",\"resource\":{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"36a25277-c394-44a3-9027-f030282343a7\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"]},\"questionnaire\":\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\",\"status\":\"completed\",\"item\":[{\"linkId\":\"numberOperableBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":250}]},{\"linkId\":\"numberOccupiedBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":221}]},{\"linkId\":\"numberOperableBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":50}]},{\"linkId\":\"numberOccupiedBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":37}]}]}}]}";
    MediaType accept = ALL;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(
                aResponse().withStatus(200).withBody("{\"resourceType\":\"OperationOutcome\"}")));

    configureFor(hlsServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathMatching(".*\\/hospital-locations.*"))
            .willReturn(
                okJson(
                    """
                        [
                            {
                                "id": 987654,
                                "ik": 987654321,
                                "label": "Testkrankenhaus - gematik GmbH",
                                "postalCode": 10117,
                                "city": "Berlin",
                                "line": "Friedrichstr.",
                                "houseNumber": "136"
                            }
                        ]""")));

    configureFor(pdfServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(500)));

    configureFor(clearingAPIServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
            .willReturn(aResponse().withStatus(200)));

    mockMvc
        .perform(post("/$process-report").headers(headers).content(body))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("information"))) // severity
        .andExpect(content().string(containsString(ALL_OK)))
        .andExpect(content().string(not(containsString("Binary"))));
  }

  @ParameterizedTest
  @MethodSource("de.gematik.demis.reportprocessingservice.RequestDataProvider#hlsCombinations")
  void shouldReturnHLSValidationError(String hlsList, String expectedError) throws Exception {
    String body =
        "<Bundle xmlns=\"http://hl7.org/fhir\"><id value=\"7a5af172-0d72-4395-8c60-c33ece5eaad2\"></id><meta><lastUpdated value=\"2022-09-05T19:06:15.141+02:00\"></lastUpdated><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\"></system><value value=\"58ab4c30-71a2-431f-a776-0febb51969f4\"></value></identifier><type value=\"document\"></type><timestamp value=\"2022-09-05T19:06:15.141+02:00\"></timestamp><entry><fullUrl value=\"https://demis.rki.de/fhir/Composition/fee47acd-4608-43f6-8b39-2fc48ce29057\"></fullUrl><resource><Composition xmlns=\"http://hl7.org/fhir\"><id value=\"fee47acd-4608-43f6-8b39-2fc48ce29057\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationId\"></system><value value=\"24919c8c-68c9-429c-9d92-2c5bce70715b\"></value></identifier><status value=\"final\"></status><type><coding><system value=\"http://loinc.org\"></system><code value=\"80563-0\"></code><display value=\"Report\"></display></coding></type><category><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportCategory\"></system><code value=\"bedOccupancyReport\"></code><display value=\"Bettenbelegungsstatistik\"></display></coding></category><subject><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\"></system><value value=\"987654\"></value></identifier></subject><date value=\"2022-09-05T19:06:15+02:00\"></date><author><reference value=\"PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></reference></author><title value=\"Bericht (Krankenhausbettenbelegungsstatistik)\"></title><section><code><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportSection\"></system><code value=\"statisticInformationBedOccupancySection\"></code><display value=\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"></display></coding></code><entry><reference value=\"QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></reference></entry></section></Composition></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></fullUrl><resource><Organization xmlns=\"http://hl7.org/fhir\"><id value=\"7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"></profile></meta><type><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/organizationType\"></system><code value=\"hospital\"></code><display value=\"Krankenhaus\"></display></coding></type><name value=\"Testkrankenhaus - gematik GmbH\"></name><telecom><system value=\"phone\"></system><value value=\"0123456789\"></value><use value=\"work\"></use></telecom><address><line value=\"Friedrichstr. 136\"></line><city value=\"Berlin\"></city><postalCode value=\"10117\"></postalCode></address></Organization></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></fullUrl><resource><PractitionerRole xmlns=\"http://hl7.org/fhir\"><id value=\"ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"></profile></meta><organization><reference value=\"Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></reference></organization></PractitionerRole></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></fullUrl><resource><QuestionnaireResponse xmlns=\"http://hl7.org/fhir\"><id value=\"1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"></profile></meta><questionnaire value=\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\"></questionnaire><status value=\"completed\"></status><item><linkId value=\"numberOperableBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"250\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"221\"></valueInteger></answer></item><item><linkId value=\"numberOperableBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"50\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"37\"></valueInteger></answer></item></QuestionnaireResponse></resource></entry></Bundle>";
    MediaType contentType = APPLICATION_XML;
    MediaType accept = ALL;

    if (accept.isWildcardType()) {
      accept = contentType;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setAccept(List.of(accept));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(200).withBody(VS_ALL_OKAY_JSON)));

    configureFor(hlsServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathMatching(".*\\/hospital-locations.*"))
            .willReturn(okJson(hlsList)));

    configureFor(pdfServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

    mockMvc
        .perform(post("/$process-report").headers(headers).content(body))
        .andExpect(status().is4xxClientError())
        .andExpect(content().string(containsString(expectedError)));
  }

  @ParameterizedTest
  @MethodSource(
      "de.gematik.demis.reportprocessingservice.RequestDataProvider#hlsCombinationsWithWeirdData")
  void shouldReturnNoHLSValidationErrors(String hlsList, String expectedError) throws Exception {
    String body =
        "<Bundle xmlns=\"http://hl7.org/fhir\"><id value=\"7a5af172-0d72-4395-8c60-c33ece5eaad2\"></id><meta><lastUpdated value=\"2022-09-05T19:06:15.141+02:00\"></lastUpdated><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\"></system><value value=\"58ab4c30-71a2-431f-a776-0febb51969f4\"></value></identifier><type value=\"document\"></type><timestamp value=\"2022-09-05T19:06:15.141+02:00\"></timestamp><entry><fullUrl value=\"https://demis.rki.de/fhir/Composition/fee47acd-4608-43f6-8b39-2fc48ce29057\"></fullUrl><resource><Composition xmlns=\"http://hl7.org/fhir\"><id value=\"fee47acd-4608-43f6-8b39-2fc48ce29057\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationId\"></system><value value=\"24919c8c-68c9-429c-9d92-2c5bce70715b\"></value></identifier><status value=\"final\"></status><type><coding><system value=\"http://loinc.org\"></system><code value=\"80563-0\"></code><display value=\"Report\"></display></coding></type><category><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportCategory\"></system><code value=\"bedOccupancyReport\"></code><display value=\"Bettenbelegungsstatistik\"></display></coding></category><subject><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\"></system><value value=\"987654\"></value></identifier></subject><date value=\"2022-09-05T19:06:15+02:00\"></date><author><reference value=\"PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></reference></author><title value=\"Bericht (Krankenhausbettenbelegungsstatistik)\"></title><section><code><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportSection\"></system><code value=\"statisticInformationBedOccupancySection\"></code><display value=\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"></display></coding></code><entry><reference value=\"QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></reference></entry></section></Composition></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></fullUrl><resource><Organization xmlns=\"http://hl7.org/fhir\"><id value=\"7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"></profile></meta><type><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/organizationType\"></system><code value=\"hospital\"></code><display value=\"Krankenhaus\"></display></coding></type><name value=\"Testkrankenhaus - gematik GmbH\"></name><telecom><system value=\"phone\"></system><value value=\"0123456789\"></value><use value=\"work\"></use></telecom><address><line value=\"Friedrichstr. 136\"></line><city value=\"Berlin\"></city><postalCode value=\"10117\"></postalCode></address></Organization></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></fullUrl><resource><PractitionerRole xmlns=\"http://hl7.org/fhir\"><id value=\"ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"></profile></meta><organization><reference value=\"Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></reference></organization></PractitionerRole></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></fullUrl><resource><QuestionnaireResponse xmlns=\"http://hl7.org/fhir\"><id value=\"1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"></profile></meta><questionnaire value=\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\"></questionnaire><status value=\"completed\"></status><item><linkId value=\"numberOperableBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"250\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"221\"></valueInteger></answer></item><item><linkId value=\"numberOperableBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"50\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"37\"></valueInteger></answer></item></QuestionnaireResponse></resource></entry></Bundle>";
    MediaType contentType = APPLICATION_XML;
    MediaType accept = ALL;

    if (accept.isWildcardType()) {
      accept = contentType;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setAccept(List.of(accept));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(200).withBody(VS_ALL_OKAY_JSON)));

    configureFor(hlsServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathMatching(".*\\/hospital-locations.*"))
            .willReturn(okJson(hlsList)));

    configureFor(pdfServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

    configureFor(clearingAPIServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
            .willReturn(aResponse().withStatus(200)));

    mockMvc
        .perform(post("/$process-report").headers(headers).content(body))
        .andExpect(status().is2xxSuccessful());
  }

  @ParameterizedTest
  @MethodSource(
      "de.gematik.demis.reportprocessingservice.RequestDataProvider#validationIssuesCombinations")
  void shouldReturnOperationOutcomeFromValdiationService(
      String body,
      MediaType contentType,
      MediaType accept,
      String validationError,
      String expectedError)
      throws Exception {
    if (accept.isWildcardType()) {
      accept = contentType;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setAccept(List.of(accept));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(422).withBody(validationError)));

    mockMvc
        .perform(post("/$process-report").headers(headers).content(body))
        .andExpect(status().is4xxClientError())
        .andExpect(header().string("Content-Type", containsString(accept.toString())))
        .andExpect(content().string(containsString(expectedError))); // bundle id
  }

  @ParameterizedTest
  @MethodSource(
      "de.gematik.demis.reportprocessingservice.RequestDataProvider#validationServiceIssuesCombinations")
  void shouldReturnOperationOutcomeWithInternalServerError(
      String body,
      MediaType contentType,
      MediaType accept,
      String validationError,
      String expectedError)
      throws Exception {
    if (accept.isWildcardType()) {
      accept = contentType;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setAccept(List.of(accept));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(500).withBody(validationError)));

    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("00000000-0000-0000-0000-000000000000");

      mockMvc
          .perform(post("/$process-report").headers(headers).content(body))
          .andExpect(status().is5xxServerError())
          .andExpect(header().string("Content-Type", containsString(accept.toString())))
          .andExpect(content().string(containsString(expectedError))); // bundle id
    }
  }

  @Test
  void givenMissingAcceptHeaderRequestWhenPostReportThenUseContentTypeAnd200JSONCase()
      throws Exception {
    Bundle report = getExampleReport();
    final String content = fhirContext.newJsonParser().encodeResourceToString(report);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(200)));

    configureFor(clearingAPIServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
            .willReturn(aResponse().withStatus(200)));

    configureFor(hlsServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathMatching(".*\\/hospital-locations.*"))
            .willReturn(
                okJson(
                        """
                        [
                            {
                                  "id":"""
                        + HOSPITAL_ID
                        +
                        """
                                ,
                                "ik": 987654321,
                                "label": "Sankt Gertrauden-Krankenhaus GmbH",
                                "postalCode": 12345,
                                "city": "Musterstadt",
                                "line": "Musterstr.",
                                "houseNumber": "1"
                            }
                        ]""")));
    configureFor(pdfServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

    MockHttpServletRequestBuilder headers1 =
        post("/$process-report").contentType(APPLICATION_JSON).content(content).headers(headers);
    mockMvc
        .perform(headers1)
        .andExpect(status().isOk())
        .andExpect(header().string(CONTENT_TYPE, APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.parameter.[*].resource.issue.[0].severity").value("information"))
        .andExpect(jsonPath("$.parameter.[*].resource.issue.[0].code").value("informational"))
        .andExpect(jsonPath("$.parameter.[*].resource.issue.[0].details.text").value(ALL_OK))
        .andExpect(content().string(containsString("TW9ja1BkZg==")));
  }

  @Test
  void givenMissingAcceptHeaderRequestWhenPostReportThenUseContentTypeAnd200XMLCase()
      throws Exception {
    Bundle report = getExampleReport();
    final String content = fhirContext.newXmlParser().encodeResourceToString(report);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(aResponse().withStatus(200)));

    configureFor(clearingAPIServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
            .willReturn(aResponse().withStatus(200)));

    configureFor(hlsServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathMatching(".*\\/hospital-locations.*"))
            .willReturn(
                okJson(
                        """
                        [
                            {
                                "id":"""
                        + HOSPITAL_ID
                        +
                        """
                                ,
                                "ik": 987654321,
                                "label": "Sankt Gertrauden-Krankenhaus GmbH",
                                "postalCode": 12345,
                                "city": "Musterstadt",
                                "line": "Musterstr.",
                                "houseNumber": "1"
                            }
                        ]""")));
    configureFor(pdfServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

    MockHttpServletRequestBuilder headers1 =
        post("/$process-report").contentType(APPLICATION_XML).content(content).headers(headers);
    mockMvc
        .perform(headers1)
        .andExpect(status().isOk())
        .andExpect(header().string(CONTENT_TYPE, APPLICATION_XML_VALUE))
        .andExpect(content().string(containsString(ALL_OK)));
  }

  private static Bundle getExampleReport() {
    final Composition compo =
        new ReportBedOccupancyDataBuilder()
            .setDefaults()
            .setSubject(new Identifier().setValue(HOSPITAL_ID))
            .build();
    Bundle report =
        new ReportBundleDataBuilder()
            .setReportBedOccupancy(compo)
            .setNotifierRole(new PractitionerRole())
            .setDefaults()
            .build();
    return report;
  }

  @Test
  void givenBlankBodyRequestWhenPostReportThen400() throws Exception {

    final String content = RandomStringUtils.randomAlphabetic(0);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(
            jsonPath("$.issue[0].diagnostics", containsString("Required request body is missing")));
  }

  @Test
  void givenUnsupportedRoleWhenPostReportThen403() throws Exception {

    OAuth2Properties properties = new OAuth2Properties();
    properties.setAllowedRoles(List.of("not-allowed-role"));
    properties.setClientId("fake-client-id");
    final String content = RandomStringUtils.randomAlphabetic(10);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(jsonPath("$.issue[0].diagnostics", containsString("Access Denied")));
  }

  @Test
  void givenNotReadableBodyRequestWithVSResponse422WhenPostReportThen422() throws Exception {

    final String content =
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
            """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setAccept(List.of(ALL));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    configureFor(validationServer.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(
                aResponse()
                    .withStatus(422)
                    .withBody(
                        """
                            {
                                "resourceType": "OperationOutcome",
                                "issue": [
                                    {
                                        "severity": "fatal",
                                        "diagnostics": "Given data is not a valid json"
                                    }
                                ]
                            }""")));

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.issue[0].severity").value("fatal"))
        .andExpect(
            jsonPath("$.issue[0].diagnostics", containsString("Given data is not a valid json")));
  }

  @Test
  void givenMissingAuthorizationHeaderRequestWhenPostReportThen401() throws Exception {

    final String content = RandomStringUtils.randomAlphabetic(5);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(
            jsonPath(
                "$.issue[0].diagnostics",
                containsString("Full authentication is required to access this resource")));
  }

  @Test
  void givenUnsupportedHTTPMethodWhenPostReportThen405() throws Exception {

    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(APPLICATION_JSON);
      headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
      headers.set(IK_NUMBER_HEADER, IK_NUMBER);
      headers.set(AZP_HEADER, AZP);

      mockMvc
          .perform(MockMvcRequestBuilders.get("/$process-report").headers(headers))
          .andExpect(status().isMethodNotAllowed())
          .andExpect(jsonPath("$.issue[0].severity").value("error"))
          .andExpect(jsonPath("$.issue[0].code").value("exception"))
          .andExpect(
              jsonPath("$.issue[0].diagnostics")
                  .value(
                      "94709a92-ee29-4b85-99a0-e9c8dd2afd3d: Request method 'GET' is not supported"));
    }
  }

  @Test
  void givenMissingContentTypeHeaderRequestWhenPostReportThen415() throws Exception {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final String content = RandomStringUtils.randomAlphabetic(5);

      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(List.of(APPLICATION_JSON));
      headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
      headers.set(IK_NUMBER_HEADER, IK_NUMBER);
      headers.set(AZP_HEADER, AZP);

      mockMvc
          .perform(post("/$process-report").headers(headers).content(content))
          .andExpect(status().isUnsupportedMediaType())
          .andExpect(jsonPath("$.issue[0].severity").value("error"))
          .andExpect(jsonPath("$.issue[0].code").value("exception"))
          .andExpect(
              jsonPath("$.issue[0].diagnostics")
                  .value("94709a92-ee29-4b85-99a0-e9c8dd2afd3d: Content-Type is not supported"));
    }
  }

  @Test
  void givenUnsupportedContentTypeWhenPostReportThen415() throws Exception {

    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final String content = RandomStringUtils.randomAlphabetic(20);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(TEXT_XML);
      headers.setAccept(List.of(APPLICATION_JSON));
      headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
      headers.set(IK_NUMBER_HEADER, IK_NUMBER);
      headers.set(AZP_HEADER, AZP);

      mockMvc
          .perform(post("/$process-report").headers(headers).content(content))
          .andExpect(status().isUnsupportedMediaType())
          .andExpect(jsonPath("$.issue[0].severity").value("error"))
          .andExpect(jsonPath("$.issue[0].code").value("exception"))
          .andExpect(
              jsonPath("$.issue[0].diagnostics")
                  .value(
                      "94709a92-ee29-4b85-99a0-e9c8dd2afd3d: Content-Type 'text/xml' is not supported"));
    }
  }

  @Test
  void givenUnsupportedAcceptWhenPostReportThen415() throws Exception {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final String content = RandomStringUtils.randomAlphabetic(20);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(APPLICATION_JSON);
      headers.setAccept(List.of(APPLICATION_PDF));
      headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
      headers.set(IK_NUMBER_HEADER, IK_NUMBER);
      headers.set(AZP_HEADER, AZP);

      mockMvc
          .perform(post("/$process-report").headers(headers).content(content))
          .andExpect(status().isNotAcceptable());
    }
  }

  @Test
  void givenUnsupportedProfileTypeWhenPostReportThen422() throws Exception {
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
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_XML);
    headers.setAccept(List.of(APPLICATION_JSON));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(
            jsonPath(
                "$.issue[0].diagnostics",
                containsString("Unexpected character '{' (code 123) in prolog; expected '<'")));
  }

  @Test
  void givenBedOccupancyExampleReturnsExpectedBundleAsString() throws Exception {

    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      utilities
          .when(Utils::getCurrentDate)
          .thenReturn(
              Date.from(
                  LocalDateTime.of(2020, 1, 1, 0, 0, 0)
                      .atZone(ZoneId.systemDefault())
                      .toInstant()));

      configureFor(validationServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
              .willReturn(
                  aResponse().withStatus(200).withBody("{\"resourceType\":\"OperationOutcome\"}")));

      configureFor(hlsServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.get(
                  urlPathMatching(".*\\/hospital-locations.*"))
              .willReturn(
                  okJson(
                      """
                          [
                              {
                                  "id": 987654,
                                  "ik": 987654321,
                                  "label": "Testkrankenhaus - gematik GmbH",
                                  "postalCode": 10117,
                                  "city": "Berlin",
                                  "line": "Friedrichstr.",
                                  "houseNumber": "136"
                              }
                          ]""")));

      configureFor(pdfServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
              .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

      configureFor(clearingAPIServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
              .willReturn(aResponse().withStatus(200)));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(APPLICATION_JSON);
      headers.setAccept(List.of(APPLICATION_JSON));
      headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
      headers.set(IK_NUMBER_HEADER, IK_NUMBER);
      headers.set(AZP_HEADER, AZP);

      String content = TestObjects.standardContent();

      String expectedContent =
          new String(
              getClass()
                  .getClassLoader()
                  .getResourceAsStream("example-reports/BedOccupancyExampleResult.json")
                  .readAllBytes(),
              StandardCharsets.UTF_8);

      var expected = jsonMapper.writeValueAsString(jsonMapper.readTree(expectedContent));

      var contentAsByteArray =
          mockMvc
              .perform(post("/$process-report").headers(headers).content(content))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsByteArray();

      String contentWithEncoding = new String(contentAsByteArray, StandardCharsets.UTF_8);

      assertThat(contentWithEncoding).isEqualTo(expected);
    }
  }

  @DisplayName("should handle error from Hls")
  @ParameterizedTest
  @CsvSource({"400,005", "500,005a"})
  void shouldHandleErrorFromHls(String hlsError, String errormessage) throws Exception {

    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      utilities
          .when(Utils::getCurrentDate)
          .thenReturn(
              Date.from(
                  LocalDateTime.of(2020, 1, 1, 0, 0, 0)
                      .atZone(ZoneId.systemDefault())
                      .toInstant()));

      configureFor(validationServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
              .willReturn(
                  aResponse().withStatus(200).withBody("{\"resourceType\":\"OperationOutcome\"}")));

      configureFor(hlsServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.get(
                  urlPathMatching(".*\\/hospital-locations.*"))
              .willReturn(aResponse().withStatus(Integer.parseInt(hlsError))));

      configureFor(pdfServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
              .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

      configureFor(clearingAPIServer.port());
      stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
              .willReturn(aResponse().withStatus(200)));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(APPLICATION_JSON);
      headers.setAccept(List.of(APPLICATION_JSON));
      headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
      headers.set(IK_NUMBER_HEADER, IK_NUMBER);
      headers.set(AZP_HEADER, AZP);

      String content = TestObjects.standardContent();

      String expectedContent =
          String.format(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: Internal Error (Error-ID-RPS-%s)\"}]}",
              errormessage);

      var contentAsByteArray =
          mockMvc
              .perform(post("/$process-report").headers(headers).content(content))
              .andExpect(status().isInternalServerError())
              .andReturn()
              .getResponse()
              .getContentAsByteArray();

      String contentWithEncoding = new String(contentAsByteArray, StandardCharsets.UTF_8);

      assertThat(contentWithEncoding).isEqualTo(expectedContent);
    }
  }

  @Test
  void shouldReturn422WithUnsupportedProfile() throws Exception {
    String content =
        """
            {
              "resourceType": "Bundle",
              "meta": {
                "profile": [
                  "https://demis.rki.de/fhir/StructureDefinition/ReportBundleUnsupported"
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
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setAccept(List.of(APPLICATION_JSON));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(
            jsonPath(
                "$.issue[0].diagnostics",
                containsString("bundle profile not supported or missing(pre-check).")));
  }
}
