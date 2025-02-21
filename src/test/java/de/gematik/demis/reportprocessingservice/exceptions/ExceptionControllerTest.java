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

package de.gematik.demis.reportprocessingservice.exceptions;

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

import ca.uhn.fhir.context.FhirContext;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.fhirparserlibrary.ParsingException;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;

class ExceptionControllerTest {

  FhirParser fhirParser = new FhirParser(FhirContext.forR4());
  ExceptionController exceptionControllerUnderTest = new ExceptionController(fhirParser);

  @DisplayName("Test bad request responses for different exceptions")
  @Test
  void testHandleBadRequestException() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");

      final MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
      when(exception.getLocalizedMessage()).thenReturn("localizedMessage");
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleBadRequestException(exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: localizedMessage\"}]}");
    }
  }

  @DisplayName("Test forbidden responses for access denied exceptions")
  @Test
  void testHandleAccessDeniedException() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");

      final AccessDeniedException exception = mock(AccessDeniedException.class);
      when(exception.getLocalizedMessage()).thenReturn("localizedMessage");
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleAccessDeniedException(exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(403));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: localizedMessage\"}]}");
    }
  }

  @DisplayName("Test unprocessable entity responses for ParsingException")
  @Test
  void testHandleUnprocessableEntityException() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final ParsingException exception = mock(ParsingException.class);
      when(exception.getLocalizedMessage()).thenReturn("localizedMessage");
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleUnprocessableEntityException(exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(422));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: localizedMessage\"}]}");
    }
  }

  @DisplayName("Test method not allowed responses for HttpRequestMethodNotSupportedException")
  @Test
  void testHandleMethodNotAllowedException() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final HttpRequestMethodNotSupportedException exception =
          mock(HttpRequestMethodNotSupportedException.class);
      when(exception.getLocalizedMessage()).thenReturn("localizedMessage");
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleMethodNotAllowedException(exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(405));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: localizedMessage\"}]}");
    }
  }

  @DisplayName("Test unsupported media type responses for HttpMediaTypeNotSupportedException")
  @Test
  void testHandleUnsupportedMediaTypeException() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final HttpMediaTypeNotSupportedException exception =
          mock(HttpMediaTypeNotSupportedException.class);
      when(exception.getLocalizedMessage()).thenReturn("localizedMessage");
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleUnsupportedMediaTypeException(exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(415));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: localizedMessage\"}]}");
    }
  }

  @DisplayName("Test internal server error responses for Exception")
  @Test
  void testHandleAllOtherExceptions() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final Exception exception = mock(Exception.class);
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleAllOtherExceptions(exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: Internal Server Error\"}]}");
    }
  }

  // test INTERNAL_SERVER_ERROR response for InternalException in handleInternalServerError method
  @DisplayName("Test internal server error responses for InternalException")
  @Test
  void testHandleInternalServerError() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final InternalException exception = mock(InternalException.class);
      final String localizedMessage = "Localized Message";
      when(exception.getLocalizedMessage()).thenReturn(localizedMessage);
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleInternalServerError(exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: Localized Message\"}]}");
    }
  }

  @DisplayName("Test unprocessable entity responses for HospitalLocationValidationException")
  @Test
  void testHandleHospitalLocationValidationException() {
    try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
      utilities.when(Utils::generateUuidString).thenReturn("94709a92-ee29-4b85-99a0-e9c8dd2afd3d");
      final HospitalLocationValidationException exception =
          mock(HospitalLocationValidationException.class);
      final String localizedMessage = "Localized Message";
      when(exception.getLocalizedMessage()).thenReturn(localizedMessage);
      final HttpServletRequest request = mock(HttpServletRequest.class);

      final ResponseEntity<String> result =
          exceptionControllerUnderTest.handleHospitalLocationValidationException(
              exception, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(422));
      assertThat(result.getBody())
          .isEqualTo(
              "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"94709a92-ee29-4b85-99a0-e9c8dd2afd3d: Localized Message\"}]}");
    }
  }
}
