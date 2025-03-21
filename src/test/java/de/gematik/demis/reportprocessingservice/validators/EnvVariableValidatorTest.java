package de.gematik.demis.reportprocessingservice.validators;

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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class EnvVariableValidatorTest {
  private final EnvVariableValidator validator = new EnvVariableValidator();

  @Test
  void testIsValidForCollection() {
    Collection<String> validCollection = Arrays.asList("VALUE1", "VALUE2", "VALUE3");
    Collection<String> invalidCollection = emptyList();

    boolean isValid1 = validator.isValid(validCollection, mock(ConstraintValidatorContext.class));
    boolean isValid2 = validator.isValid(invalidCollection, mock(ConstraintValidatorContext.class));

    assertThat(isValid1).isTrue();
    assertThat(isValid2).isFalse();
  }

  @Test
  void testIsValidForString() {
    String validEnvVariable = "VALID_VALUE";
    String invalidEnvVariable = null;

    boolean isValid1 = validator.isValid(validEnvVariable, mock(ConstraintValidatorContext.class));
    boolean isValid2 =
        validator.isValid(invalidEnvVariable, mock(ConstraintValidatorContext.class));

    assertThat(isValid1).isTrue();
    assertThat(isValid2).isFalse();
  }
}
