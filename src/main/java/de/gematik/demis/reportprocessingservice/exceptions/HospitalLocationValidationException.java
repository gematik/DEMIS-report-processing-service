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

import static java.lang.String.format;

import lombok.Getter;

@Getter
public class HospitalLocationValidationException extends RuntimeException {

  public static final String VALIDATION_EXCEPTION_FOR_ID_S_NO_INEK_DATA_FOUND =
      "Validation Exception (for id %s no InEK data found)";
  public static final String VALIDATION_EXCEPTION_ENTERED_DATA_S_IN_EK_DATA_S =
      "Validation Exception (Entered data: %s | InEK Data: %s)";
  private final String msg;

  public HospitalLocationValidationException(String enteredDId) {
    super(format(VALIDATION_EXCEPTION_FOR_ID_S_NO_INEK_DATA_FOUND, enteredDId));
    msg = format(VALIDATION_EXCEPTION_FOR_ID_S_NO_INEK_DATA_FOUND, enteredDId);
  }

  public HospitalLocationValidationException(String enteredData, String expectedData) {
    super(format(VALIDATION_EXCEPTION_ENTERED_DATA_S_IN_EK_DATA_S, enteredData, expectedData));
    msg = format(VALIDATION_EXCEPTION_ENTERED_DATA_S_IN_EK_DATA_S, enteredData, expectedData);
  }

  @Override
  public String getLocalizedMessage() {
    return msg;
  }
}
