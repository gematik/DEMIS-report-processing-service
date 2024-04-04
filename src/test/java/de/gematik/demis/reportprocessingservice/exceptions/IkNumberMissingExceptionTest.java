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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IkNumberMissingExceptionTest {

  @Test
  void testDefaultConstructor() {
    IkNumberMissingException exception = new IkNumberMissingException();
    assertThat(exception.getLocalizedMessage()).isEqualTo("IkNumber in Token is missing");
  }

  @Test
  void testCustomMessageConstructor() {
    String customMessage = "Custom IkNumber message";
    IkNumberMissingException exception = new IkNumberMissingException(customMessage);
    assertThat(exception.getLocalizedMessage()).isEqualTo(customMessage);
  }
}
