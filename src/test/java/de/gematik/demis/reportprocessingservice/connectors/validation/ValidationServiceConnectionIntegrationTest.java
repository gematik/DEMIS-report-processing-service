package de.gematik.demis.reportprocessingservice.connectors.validation;

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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import de.gematik.demis.fhirparserlibrary.MessageType;
import de.gematik.demis.reportprocessingservice.exceptions.RestClientException;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    properties = {
      "demis.network.validation-service-address=http://localhost:${wiremock.server.port}/VS",
      "feature.flag.relaxed.validation=false"
    })
@AutoConfigureWireMock(port = 0)
@EnableAutoConfiguration(exclude = {SpringDocConfiguration.class})
class ValidationServiceConnectionIntegrationTest {
  private static final String ENDPOINT_VS = "/VS/$validate";
  private static final String REQUEST_BODY = "my body";
  private static final String RESPONSE_BODY =
      """
                    {"outcome":"does not matter"}
            """;

  @MockitoBean FhirContext fhirContext;
  @Autowired ValidationServiceConnectionService underTest;

  private static void setupVS(
      final String contentType, final ResponseDefinitionBuilder responseDefBuilder) {
    stubFor(
        post(ENDPOINT_VS)
            .withHeader(CONTENT_TYPE, equalTo(contentType))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
            .withRequestBody(equalTo(REQUEST_BODY))
            .willReturn(responseDefBuilder));
  }

  private static MediaType getMediaType(final MessageType messageType) {
    return switch (messageType) {
      case JSON -> APPLICATION_JSON;
      case XML -> APPLICATION_XML;
    };
  }

  private IParser setupFhirJsonParserMock() {
    final IParser parser = Mockito.mock(IParser.class);
    when(fhirContext.newJsonParser()).thenReturn(parser);
    return parser;
  }

  private OperationOutcome mockParseOutcomeForResponse(final String stringToParse) {
    final IParser fhirJsonParser = setupFhirJsonParserMock();
    final OperationOutcome outcome = new OperationOutcome();
    outcome.setId("just for testing");
    when(fhirJsonParser.parseResource(OperationOutcome.class, stringToParse)).thenReturn(outcome);
    return outcome;
  }

  @ParameterizedTest
  @EnumSource(MessageType.class)
  void validationOkay(final MessageType messageType) {
    final MediaType contentType = getMediaType(messageType);
    setupVS(contentType.toString(), okJson(RESPONSE_BODY));
    final OperationOutcome outcome = mockParseOutcomeForResponse(RESPONSE_BODY);

    final ValidationResult result = underTest.validateBundle(contentType, REQUEST_BODY);

    assertThat(result)
        .isNotNull()
        .returns(true, ValidationResult::isValid)
        .returns(outcome, ValidationResult::operationOutcome);
  }

  @ParameterizedTest
  @EnumSource(MessageType.class)
  void validationErrorOutcome(final MessageType messageType) {
    final var contentType = getMediaType(messageType);

    setupVS(
        contentType.toString(),
        WireMock.status(422)
            .withBody(RESPONSE_BODY)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

    final OperationOutcome outcome = mockParseOutcomeForResponse(RESPONSE_BODY);
    outcome.addIssue().setSeverity(ERROR);

    final ValidationResult result = underTest.validateBundle(contentType, REQUEST_BODY);

    assertThat(result)
        .isNotNull()
        .returns(false, ValidationResult::isValid)
        .returns(outcome, ValidationResult::operationOutcome);
  }

  @Test
  void validationCallException() {
    setupVS(APPLICATION_JSON_VALUE, WireMock.serverError());
    assertThatThrownBy(() -> underTest.validateBundle(APPLICATION_JSON, REQUEST_BODY))
        .isExactlyInstanceOf(RestClientException.class);
  }
}
