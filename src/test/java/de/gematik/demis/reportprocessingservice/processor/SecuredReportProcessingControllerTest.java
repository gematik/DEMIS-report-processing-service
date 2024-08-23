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

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.IDPFaker;
import de.gematik.demis.reportprocessingservice.properties.OAuth2Properties;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.exceptions.base.MockitoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureWireMock(port = 0)
class SecuredReportProcessingControllerTest {

  private static final String IK_NUMBER_HEADER = "IK-Number";
  private static final String IK_NUMBER = "987654321";
  private static final String AZP_HEADER = "azp";
  private static final String AZP = "demis-test";

  @Autowired private MockMvc mockMvc;

  @MockBean private ReportProcessingService reportProcessingService;
  @MockBean private FhirParser fhirParserService;

  @MockBean private OAuth2Properties oAuth2Properties;

  @Value("${wiremock-issuer-uri}")
  private String issuerUri;

  private static final IDPFaker idpFaker = new IDPFaker();
  private static final String OUTCOME_PATTERN =
      "{\"resourceType\": \"OperationOutcome\",\"issue\": [{\"severity\": \"error\",\"code\": \"exception\","
          + "\"diagnostics\": \"%s\"}]}";

  @BeforeEach
  public void setUp() {
    when(oAuth2Properties.getClientId()).thenReturn("client-id");
    when(oAuth2Properties.getAllowedRoles()).thenReturn(List.of("role"));
    idpFaker.fakingServer(issuerUri);
  }

  @ParameterizedTest
  @ValueSource(strings = {APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
  void givenValidRequestWhenPostReportThen200(String contentType) throws Exception {

    final MediaType mediaType = MediaType.parseMediaType(contentType);
    final String content = RandomStringUtils.randomAlphabetic(20);
    when(reportProcessingService.process(
            anyString(),
            any(MediaType.class),
            any(),
            any(MediaType.class),
            anyString(),
            anyString(),
            anyString()))
        .thenReturn(ResponseEntity.ok().body(content));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(mediaType);
    headers.setAccept(List.of(mediaType));
    headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", containsString(contentType)))
        .andExpect(content().string(content));
  }

  @Test
  void givenBlankBodyRequestWhenPostReportThen400() throws Exception {

    String message = "Required request body is missing";
    when(fhirParserService.encode(any(), anyString()))
        .thenReturn(String.format(OUTCOME_PATTERN, message));
    final String content = RandomStringUtils.randomAlphabetic(0);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(jsonPath("$.issue[0].diagnostics", containsString(message)));
  }

  @Test
  void givenMissingAuthorizationHeaderRequestWhenPostReportThen403() throws Exception {

    final String content = RandomStringUtils.randomAlphabetic(5);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);

    mockMvc
        .perform(post("/$process-report").headers(headers).content(content))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenUnsupportedHTTPMethodWhenPostReportThen405() throws Exception {

    String message = "Request method 'GET' not supported";
    when(fhirParserService.encode(any(), anyString()))
        .thenReturn(String.format(OUTCOME_PATTERN, message));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);

    mockMvc
        .perform(get("/$process-report").headers(headers))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(jsonPath("$.issue[0].diagnostics", containsString(message)));
  }

  @Test
  void givenMissingContentTypeHeaderRequestWhenPostReportThen415() throws Exception {

    String message = "Request method 'GET' not supported";
    when(fhirParserService.encode(any(), anyString()))
        .thenReturn(String.format(OUTCOME_PATTERN, message));
    final String content = RandomStringUtils.randomAlphabetic(5);
    when(oAuth2Properties.getClientId()).thenReturn("client-id");
    when(oAuth2Properties.getAllowedRoles()).thenReturn(List.of("role"));
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
        .andExpect(jsonPath("$.issue[0].diagnostics", containsString(message)));
  }

  @Test
  void givenUnsupportedContentTypeWhenPostReportThen415() throws Exception {

    String message = "Content type 'text/xml' not supported";
    when(fhirParserService.encode(any(), anyString()))
        .thenReturn(String.format(OUTCOME_PATTERN, message));
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
        .andExpect(jsonPath("$.issue[0].diagnostics", containsString(message)));
  }

  @Test
  void givenMockedExceptionWhenPostReportThen500() throws Exception {

    final String message = RandomStringUtils.randomAlphabetic(10);
    when(fhirParserService.encode(any(), anyString()))
        .thenReturn(String.format(OUTCOME_PATTERN, message));
    Exception exception = new MockitoException(message);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_XML);
    headers.setAccept(List.of(APPLICATION_JSON));
    headers.setBearerAuth(idpFaker.fakingToken(issuerUri, oAuth2Properties));
    headers.set(IK_NUMBER_HEADER, IK_NUMBER);
    headers.set(AZP_HEADER, AZP);
    doThrow(exception)
        .when(reportProcessingService)
        .process(
            anyString(),
            any(MediaType.class),
            any(),
            any(MediaType.class),
            anyString(),
            anyString(),
            anyString());

    mockMvc
        .perform(post("/$process-report").headers(headers).content(message))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.issue[0].severity").value("error"))
        .andExpect(jsonPath("$.issue[0].code").value("exception"))
        .andExpect(jsonPath("$.issue[0].diagnostics", containsString(message)));
  }
}
