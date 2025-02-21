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

import static de.gematik.demis.reportprocessingservice.internal.OperationOutcomeUtils.exceptionOperationOutcome;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.fhirparserlibrary.ParsingException;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionController {

  private final FhirParser fhirParser;

  @ExceptionHandler(
      value = {
        MissingRequestHeaderException.class,
        HttpMessageNotReadableException.class,
        IkNumberMissingException.class
      })
  public ResponseEntity<String> handleBadRequestException(
      Exception exception, HttpServletRequest request) {

    return createResponseEntity(exception, BAD_REQUEST, request, exception.getLocalizedMessage());
  }

  @ExceptionHandler(value = AccessDeniedException.class)
  public ResponseEntity<String> handleAccessDeniedException(
      Exception exception, HttpServletRequest request) {

    return createResponseEntity(exception, FORBIDDEN, request, exception.getLocalizedMessage());
  }

  @ExceptionHandler(value = {DataFormatException.class, ParsingException.class})
  public ResponseEntity<String> handleUnprocessableEntityException(
      Exception exception, HttpServletRequest request) {

    return createResponseEntity(
        exception, UNPROCESSABLE_ENTITY, request, exception.getLocalizedMessage());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<String> handleMethodNotAllowedException(
      Exception exception, HttpServletRequest request) {

    return createResponseEntity(
        exception, METHOD_NOT_ALLOWED, request, exception.getLocalizedMessage());
  }

  @ExceptionHandler({
    HttpMediaTypeNotSupportedException.class,
    HttpMediaTypeNotAcceptableException.class
  })
  public ResponseEntity<String> handleUnsupportedMediaTypeException(
      Exception exception, HttpServletRequest request) {

    return createResponseEntity(
        exception, UNSUPPORTED_MEDIA_TYPE, request, exception.getLocalizedMessage());
  }

  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<String> handleAllOtherExceptions(
      Exception exception, HttpServletRequest request) {

    return createResponseEntity(
        exception, INTERNAL_SERVER_ERROR, request, INTERNAL_SERVER_ERROR.getReasonPhrase());
  }

  @ExceptionHandler(value = {InternalException.class})
  public ResponseEntity<String> handleInternalServerError(
      InternalException exception, HttpServletRequest request) {

    return createResponseEntity(
        exception, INTERNAL_SERVER_ERROR, request, exception.getLocalizedMessage());
  }

  @ExceptionHandler(value = {HospitalLocationValidationException.class})
  public ResponseEntity<String> handleHospitalLocationValidationException(
      HospitalLocationValidationException exception, HttpServletRequest request) {

    return createResponseEntity(
        exception, UNPROCESSABLE_ENTITY, request, exception.getLocalizedMessage());
  }

  @ExceptionHandler(value = {RpsServiceException.class})
  public ResponseEntity<String> handleRpsServiceException(
      RpsServiceException exception, HttpServletRequest request) {

    return createResponseEntity(
        exception, UNPROCESSABLE_ENTITY, request, exception.getLocalizedMessage());
  }

  public ResponseEntity<String> createResponseEntity(
      Exception exception, HttpStatus httpStatus, HttpServletRequest request, String info) {

    String logUuid = Utils.generateUuidString();
    String logMessage =
        String.format("%s Error processing request %s", logUuid, request.getPathInfo());
    if (httpStatus.is5xxServerError()) {
      log.error(logMessage, exception);
    } else {
      log.info(logMessage, exception);
    }

    final MediaType mediaType = getMediaType(request);
    final OperationOutcome outcome = exceptionOperationOutcome(logUuid + ": " + info);
    return ResponseEntity.status(httpStatus)
        .contentType(mediaType)
        .body(fhirParser.encode(outcome, mediaType.getSubtype()));
  }

  private MediaType getMediaType(HttpServletRequest request) {
    String accept = request.getHeader(ACCEPT);
    if (StringUtils.isBlank(accept) || accept.contains("*")) {
      accept = request.getHeader(CONTENT_TYPE);
    }
    return accept == null ? MediaType.APPLICATION_JSON : MediaType.parseMediaType(accept);
  }
}
