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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HospitalLocationValidationExceptionTest {

  @Test
  void testValidationExceptionForIdNoInEkDataFound() {
    String enteredId = "123";

    HospitalLocationValidationException exception =
        new HospitalLocationValidationException(enteredId);

    assertThat(exception.getLocalizedMessage())
        .isEqualTo(String.format("Validation Exception (for id %s no InEK data found)", enteredId));
  }

  @Test
  void testValidationExceptionEnteredDataInEkData() {
    String enteredData = "Data1";
    String expectedData = "Data2";

    HospitalLocationValidationException exception =
        new HospitalLocationValidationException(enteredData, expectedData);

    assertThat(exception.getLocalizedMessage())
        .isEqualTo(
            String.format(
                "Validation Exception (Entered data: %s | InEK Data: %s)",
                enteredData, expectedData));
  }
}
