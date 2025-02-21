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

import de.gematik.demis.reportprocessingservice.utils.ErrorCode;
import de.gematik.demis.service.base.error.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RpsServiceExceptionTest {

  @Test
  void shouldCreateExceptionWithResponseStatus() {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String errorCode = "ERROR_CODE";
    String message = "Error message";

    ServiceException exception = new RpsServiceException(status, errorCode, message);

    assertThat(exception).hasMessage(message);
    assertThat(exception.getErrorCode()).isEqualTo(errorCode);
    assertThat(exception.getResponseStatus()).isEqualTo(status);
  }

  @Test
  void shouldCreateExceptionWithErrorCode() {
    ErrorCode errorCode = ErrorCode.ERROR_IN_VALIDATION_CALL; // replace with a valid ErrorCode
    String message = "Error message";

    ServiceException exception = new RpsServiceException(errorCode, message);

    assertThat(exception).hasMessage(message);
    assertThat(exception.getErrorCode()).isEqualTo(errorCode.asString());
    assertThat(exception.getResponseStatus()).isEqualTo(errorCode.getHttpStatus());
  }
}
