package de.gematik.demis.reportprocessingservice.utils;

/*-
 * #%L
 * report-processing-service
 * %%
 * Copyright (C) 2025 - 2026 gematik GmbH
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
 * For additional notes and disclaimer from gematik and in case of changes by gematik,
 * find details in the "Readme" file.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class CustomResponseEntityTest {

  @Test
  void shouldCreateEntityWithStatus() {
    CustomResponseEntity customResponseEntity = new CustomResponseEntity(HttpStatus.ACCEPTED);
    assertThat(customResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(customResponseEntity.getBody()).isNull();
    assertThat(customResponseEntity.getHeaders().toSingleValueMap()).isEmpty();
  }

  @Test
  void shouldCreateEntityWithStatusAndHeader() {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.ACCEPT, "headerValue1");
    CustomResponseEntity customResponseEntity =
        new CustomResponseEntity(headers, HttpStatus.ACCEPTED);
    assertThat(customResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(customResponseEntity.getBody()).isNull();
    assertThat(customResponseEntity.getHeaders().toSingleValueMap())
        .containsEntry(HttpHeaders.ACCEPT, "headerValue1");
  }

  @Test
  void shouldCreateEntityWithStatusAndHeaderAndBody() {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.ACCEPT, "headerValue1");
    String body = "body";
    CustomResponseEntity customResponseEntity =
        new CustomResponseEntity(body, headers, HttpStatus.ACCEPTED);
    assertThat(customResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(customResponseEntity.getHeaders().toSingleValueMap())
        .containsEntry(HttpHeaders.ACCEPT, "headerValue1");
    assertThat(customResponseEntity.getBody()).isEqualTo(body);
  }
}
