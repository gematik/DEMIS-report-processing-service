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

package de.gematik.demis.reportprocessingservice.security.filters;

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
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Utils;
import de.gematik.demis.reportprocessingservice.exceptions.ExceptionController;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {
  private final FhirParser fhirParserService = new FhirParser(FhirContext.forR4());
  private final ExceptionController exceptionController =
      new ExceptionController(fhirParserService);

  @Test
  void givenAuthenticationErrorWhenCommenceThen401() throws IOException {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final String message = RandomStringUtils.randomAlphabetic(5);
      final IParser jsonParser = FhirContext.forR4().newJsonParser();
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getHeader(ACCEPT)).thenReturn(APPLICATION_JSON_VALUE);
      MockHttpServletResponse response = new MockHttpServletResponse();
      AuthenticationException exception = new AuthenticationServiceException(message);
      final CustomAuthenticationEntryPoint authenticationEntryPoint =
          new CustomAuthenticationEntryPoint(exceptionController);

      authenticationEntryPoint.commence(request, response, exception);

      assertThat(response)
          .isNotNull()
          .hasFieldOrPropertyWithValue("status", UNAUTHORIZED.value())
          .hasFieldOrPropertyWithValue("contentType", APPLICATION_JSON_VALUE);
      final OperationOutcome outcome =
          jsonParser.parseResource(OperationOutcome.class, response.getContentAsString());

      assertThat(outcome.getIssue())
          .isNotEmpty()
          .hasSize(1)
          .element(0)
          .hasFieldOrPropertyWithValue("severity", OperationOutcome.IssueSeverity.ERROR)
          .hasFieldOrPropertyWithValue("code", OperationOutcome.IssueType.EXCEPTION)
          .hasFieldOrPropertyWithValue(
              "diagnostics", "94709a92-ee29-4b85-99a0-e9c8dd2afd3d: " + message);
    }
  }
}
