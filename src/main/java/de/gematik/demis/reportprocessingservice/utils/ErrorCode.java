package de.gematik.demis.reportprocessingservice.utils;

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

import static java.lang.String.format;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * always remember to update the confluence documentation under {@link
 * https://wiki.gematik.de/x/Ym1CH}
 */
public enum ErrorCode {
  ERROR_IN_VALIDATION_CALL("000"),
  FIRST_ELEMENT_IN_BUNDLE_ENTRY_NOT_COMPOSITION("001"),
  NO_LOCATION_DATA_MATCHING_INPUT_FOUND("002"),
  ERROR_IN_NCAPI_CALL("003"),
  ERROR_IN_NCAPI_CALL_5XX("003a"),
  ERROR_IN_PDF_CALL("004"),
  ERROR_IN_PDF_CALL_5XX("004a"),
  ERROR_IN_HLS_CALL("005"),
  ERROR_IN_HLS_CALL_5XX("005a"),
  PROFILE_NOT_SUPPORTED("006", HttpStatus.UNPROCESSABLE_ENTITY);

  private final String id;
  @Getter private final HttpStatus httpStatus;

  ErrorCode(String id) {
    this(id, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  ErrorCode(String id, HttpStatus httpStatus) {
    this.id = id;
    this.httpStatus = httpStatus;
  }

  public String asString() {
    return format("Error-ID-RPS-%s", id);
  }
}
