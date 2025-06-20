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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Taken and striped from
 * https://raw.githubusercontent.com/eugenp/tutorials/eb633a5b19658f8c2afc176c4dfc5510540ed10d/core-java-modules/core-java-uuid/src/test/java/com/baeldung/uuid/UUIDGeneratorUnitTest.java
 */
class UUID5GeneratorTest {

  @Test
  void uuid5IsCorrectlyGeneratedForDomainBaeldungName() {

    UUID uuid = UUID5Generator.generateType5UUID("baeldung.com");

    assertThat(uuid).hasToString("efd5462b-b07a-52a3-94ea-bf575c0e0e75");
    assertThat(uuid.version()).isEqualTo(5);
    assertThat(uuid.variant()).isEqualTo(2);
  }

  @Test
  void testGenerateType5UUID() {
    String name = "test-name";

    try (MockedStatic<MessageDigest> utilities = Mockito.mockStatic(MessageDigest.class)) {
      utilities
          .when(() -> MessageDigest.getInstance("SHA-1"))
          .thenThrow(new NoSuchAlgorithmException());
      assertThatThrownBy(() -> UUID5Generator.generateType5UUID(name))
          .isInstanceOf(AssertionError.class);
    }
  }
}
