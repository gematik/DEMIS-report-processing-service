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

package de.gematik.demis.reportprocessingservice.connectors.hls;

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

import static de.gematik.demis.reportprocessingservice.connectors.hls.HospitalLocationConnectionService.MISSING_OR_INVALID_IK_NUMBER;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.demis.reportprocessingservice.exceptions.IkNumberMissingException;
import de.gematik.demis.reportprocessingservice.internal.HospitalLocationDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class HospitalLocationConnectionServiceTest {

  @InjectMocks HospitalLocationConnectionService hospitalLocationConnectionService;
  @Mock private HospitalLocationServiceClient hospitalLocationServiceClientMock;

  @Test
  void shouldCallClient() {
    when(hospitalLocationServiceClientMock.getHospitalData(anyString()))
        .thenReturn(
            ResponseEntity.ok()
                .body(singletonList(HospitalLocationDTO.builder().ik("987654321").build())));
    hospitalLocationConnectionService.getHospitalData("987654321");

    verify(hospitalLocationServiceClientMock).getHospitalData("987654321");
  }

  @Test
  void shouldReturnEmptyListIfHlsReturnEmptyBody() {
    when(hospitalLocationServiceClientMock.getHospitalData(anyString()))
        .thenReturn(ResponseEntity.ok().build());
    List<HospitalLocationDTO> hospitalData =
        hospitalLocationConnectionService.getHospitalData("987654321");

    assertThat(hospitalData).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"-123456789", "aBc1DEF2x", "9876543210"})
  void shouldThrowExceptionForInvalidIkNumber(String ik) {
    assertThatThrownBy(() -> hospitalLocationConnectionService.getHospitalData(ik))
        .isInstanceOf(IkNumberMissingException.class)
        .hasMessage(MISSING_OR_INVALID_IK_NUMBER);
  }
}
