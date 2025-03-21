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
 * #L%
 */

import static org.springframework.util.StringUtils.hasText;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {

  /**
   * verify if the given property is an environment variable
   *
   * @param property some property
   * @return true or false
   */
  public static boolean isValidEnvVariable(String property) {
    return hasText(property) && !property.startsWith("${") && !property.endsWith("}");
  }
}
