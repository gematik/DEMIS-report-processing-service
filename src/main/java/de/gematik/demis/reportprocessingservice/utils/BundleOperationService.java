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

import java.util.Date;
import java.util.Optional;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

/** Service to handle operations on a FHIR Bundle */
@Service
public class BundleOperationService {

  /**
   * Get the composition from a bundle if present
   *
   * @param bundle the bundle to search in
   * @return the Optional<Composition>
   */
  public Optional<Composition> getComposition(Bundle bundle) {
    Optional<Resource> first =
        bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(Composition.class::isInstance)
            .findFirst();
    return first.map(Composition.class::cast);
  }

  /**
   * Add an entry to a bundle and update the lastUpdated field
   *
   * @param bundle the bundle to add the entry to
   * @param entry the entry to add
   */
  public void addEntry(Bundle bundle, BundleEntryComponent entry) {
    bundle.addEntry(entry);
    updated(bundle);
  }

  /**
   * Update the lastUpdated field of all given resources
   *
   * @param resources
   */
  public void updated(final IBaseResource... resources) {
    for (final var resource : resources) {
      resource.getMeta().setLastUpdated(new Date());
    }
  }
}
