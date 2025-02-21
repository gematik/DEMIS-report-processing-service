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

import de.gematik.demis.reportprocessingservice.exceptions.IkNumberMissingException;
import de.gematik.demis.reportprocessingservice.internal.HospitalLocationDTO;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HospitalLocationConnectionService {

  public static final String WHITE_SPACES_REGEX = "\\s+";
  public static final String EXACTLY_9_NUMBERS_REGEX = "^\\d{9}";
  public static final String MISSING_OR_INVALID_IK_NUMBER = "missing or invalid ik number";

  private final HospitalLocationServiceClient hospitalLocationServiceClient;

  public List<HospitalLocationDTO> getHospitalData(String ik) {
    // clean white spaces
    if (ik != null) {
      ik = ik.replaceAll(WHITE_SPACES_REGEX, "");
    }
    if (!isValidIk(ik)) {
      throw new IkNumberMissingException(MISSING_OR_INVALID_IK_NUMBER);
    }
    log.info("sending number {} to hls", ik);
    ResponseEntity<List<HospitalLocationDTO>> hospitalDataResponseEntity =
        hospitalLocationServiceClient.getHospitalData(ik);
    List<HospitalLocationDTO> hospitalLocations = hospitalDataResponseEntity.getBody();
    if (hospitalLocations != null) {
      log.info("received hospital location data with {} entries", hospitalLocations.size());
      return hospitalLocations;
    }
    return Collections.emptyList();
  }

  private boolean isValidIk(String ik) {
    if (StringUtils.isBlank(ik)) {
      log.error("missing ik number");
      return false;
    }
    final boolean matches = Pattern.matches(EXACTLY_9_NUMBERS_REGEX, ik);
    if (!matches) {
      log.error("the ik does not match the specified pattern");
    }
    return matches;
  }
}
