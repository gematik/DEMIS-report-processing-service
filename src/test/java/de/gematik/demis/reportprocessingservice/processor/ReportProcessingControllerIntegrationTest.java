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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static de.gematik.demis.reportprocessingservice.utils.Constants.ALL_OK;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc()
@SpringBootTest
@AutoConfigureWireMock(port = 0) // dynamic random port
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReportProcessingControllerIntegrationTest {

  private static final WireMockServer VALIDATION_SERVER = new WireMockServer(7070);
  private static final WireMockServer CLEARING_API_SERVER = new WireMockServer(7071);
  private static final WireMockServer HLS_SERVER = new WireMockServer(7072);
  private static final WireMockServer PDF_SERVER = new WireMockServer(7073);
  private static final UrlPathPattern HLS_GET_URL = urlPathMatching(".*\\/hospital-locations.*");

  @BeforeAll
  public static void startServers() {
    VALIDATION_SERVER.start();
    CLEARING_API_SERVER.start();
    HLS_SERVER.start();
    PDF_SERVER.start();
  }

  @AfterAll
  public static void stopServers() {
    VALIDATION_SERVER.stop();
    CLEARING_API_SERVER.stop();
    HLS_SERVER.stop();
    PDF_SERVER.stop();
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void givenValidRequestWithIkNumberWhenPostReportThen200() throws Exception {
    String body =
        "<Bundle xmlns=\"http://hl7.org/fhir\"><id value=\"7a5af172-0d72-4395-8c60-c33ece5eaad2\"></id><meta><lastUpdated value=\"2022-09-05T19:06:15.141+02:00\"></lastUpdated><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\"></system><value value=\"58ab4c30-71a2-431f-a776-0febb51969f4\"></value></identifier><type value=\"document\"></type><timestamp value=\"2022-09-05T19:06:15.141+02:00\"></timestamp><entry><fullUrl value=\"https://demis.rki.de/fhir/Composition/fee47acd-4608-43f6-8b39-2fc48ce29057\"></fullUrl><resource><Composition xmlns=\"http://hl7.org/fhir\"><id value=\"fee47acd-4608-43f6-8b39-2fc48ce29057\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationId\"></system><value value=\"24919c8c-68c9-429c-9d92-2c5bce70715b\"></value></identifier><status value=\"final\"></status><type><coding><system value=\"http://loinc.org\"></system><code value=\"80563-0\"></code><display value=\"Report\"></display></coding></type><category><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportCategory\"></system><code value=\"bedOccupancyReport\"></code><display value=\"Bettenbelegungsstatistik\"></display></coding></category><subject><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\"></system><value value=\"987654\"></value></identifier></subject><date value=\"2022-09-05T19:06:15+02:00\"></date><author><reference value=\"PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></reference></author><title value=\"Bericht (Krankenhausbettenbelegungsstatistik)\"></title><section><code><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportSection\"></system><code value=\"statisticInformationBedOccupancySection\"></code><display value=\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"></display></coding></code><entry><reference value=\"QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></reference></entry></section></Composition></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></fullUrl><resource><Organization xmlns=\"http://hl7.org/fhir\"><id value=\"7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"></profile></meta><type><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/organizationType\"></system><code value=\"hospital\"></code><display value=\"Krankenhaus\"></display></coding></type><name value=\"Testkrankenhaus - gematik GmbH\"></name><telecom><system value=\"phone\"></system><value value=\"0123456789\"></value><use value=\"work\"></use></telecom><address><line value=\"Friedrichstr. 136\"></line><city value=\"Berlin\"></city><postalCode value=\"10117\"></postalCode></address></Organization></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></fullUrl><resource><PractitionerRole xmlns=\"http://hl7.org/fhir\"><id value=\"ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"></profile></meta><organization><reference value=\"Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></reference></organization></PractitionerRole></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></fullUrl><resource><QuestionnaireResponse xmlns=\"http://hl7.org/fhir\"><id value=\"1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"></profile></meta><questionnaire value=\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\"></questionnaire><status value=\"completed\"></status><item><linkId value=\"numberOperableBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"250\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"221\"></valueInteger></answer></item><item><linkId value=\"numberOperableBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"50\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"37\"></valueInteger></answer></item></QuestionnaireResponse></resource></entry></Bundle>";
    MediaType contentType = MediaType.APPLICATION_XML;
    MediaType accept = MediaType.ALL;
    if (accept.isWildcardType()) {
      accept = contentType;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.setAccept(List.of(accept));
    headers.set(ReportProcessingController.AZP_HEADER, "Burghain-Hospitals");
    headers.set(ReportProcessingController.IK_NUMBER_HEADER, "987654321");

    configureMockServers();

    mockMvc
        .perform(post("/$process-report").headers(headers).content(body))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", containsString(accept.toString())))
        .andExpect(content().string(containsString("information"))) // severity
        .andExpect(content().string(containsString(ALL_OK)))
        .andExpect(content().string(containsString("TW9ja1BkZg==")));

    HLS_SERVER.verify(WireMock.getRequestedFor(HLS_GET_URL));
  }

  private void configureMockServers() {
    configureFor(VALIDATION_SERVER.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/\\$validate"))
            .willReturn(
                aResponse().withStatus(200).withBody("{\"resourceType\": \"OperationOutcome\"}")));

    configureFor(HLS_SERVER.port());
    stubFor(
        get(HLS_GET_URL)
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

    configureFor(PDF_SERVER.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/bedOccupancy"))
            .willReturn(aResponse().withStatus(200).withBody("MockPdf".getBytes())));

    configureFor(CLEARING_API_SERVER.port());
    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.post(urlPathMatching(".*\\/"))
            .willReturn(aResponse().withStatus(200)));
  }
}
