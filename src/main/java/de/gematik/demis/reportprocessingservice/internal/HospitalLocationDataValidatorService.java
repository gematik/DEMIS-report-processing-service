package de.gematik.demis.reportprocessingservice.internal;

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

import de.gematik.demis.reportprocessingservice.connectors.hls.HospitalLocationConnectionService;
import de.gematik.demis.reportprocessingservice.exceptions.HospitalLocationValidationException;
import de.gematik.demis.reportprocessingservice.exceptions.InternalException;
import de.gematik.demis.reportprocessingservice.utils.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class HospitalLocationDataValidatorService {

  private final HospitalLocationConnectionService hospitalLocationConnectionService;

  public void validateLocationData(String ikNumber, Bundle bundle) {
    log.debug("Validating responsibility for hospital locations. IK: {}", ikNumber);

    // extract relevant data from report
    Composition composition = getComposition(bundle);

    // get List from HLS
    List<HospitalLocationDTO> hospitalLocations =
        hospitalLocationConnectionService.getHospitalData(ikNumber);

    String locationIdFromReport = composition.getSubject().getIdentifier().getValue();
    // check id from bundle
    if (hospitalLocations.stream()
        .map(HospitalLocationDTO::getLocationId)
        .toList()
        .contains(locationIdFromReport)) {
      return;
    }
    throw new HospitalLocationValidationException(locationIdFromReport);
  }

  private Composition getComposition(Bundle bundle) {
    Resource resource = bundle.getEntryFirstRep().getResource();
    if (!(resource instanceof Composition)) {
      throw new InternalException(ErrorCode.FIRST_ELEMENT_IN_BUNDLE_ENTRY_NOT_COMPOSITION);
    }
    return (Composition) resource;
  }
}
