package de.gematik.demis.reportprocessingservice.connectors.ces;

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

import static de.gematik.demis.reportprocessingservice.utils.Constants.PROFILE_BASE_URL;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.utils.BundleOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Provenance;
import org.springframework.stereotype.Service;

/**
 * Service used to connect to the context enrichment service and enrich the bundle with context
 * information
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextEnrichmentService {

  private final ContextEnrichmentServiceClient contextEnrichmentServiceClient;
  private final FhirParser fhirParserService;
  private final BundleOperationService bundleOperationService;

  /**
   * Enriches the bundle with context information
   *
   * @param bundle the bundle to enrich
   * @param authorization the authorization header
   * @return the enriched bundle
   */
  public void enrichBundleWithContextInformation(Bundle bundle, String authorization) {
    if (isBlank(authorization)) {
      log.warn("Authorization is null but required by CES. No enrichment with CES!");
      return;
    }
    try {
      final Composition composition =
          bundleOperationService
              .getComposition(bundle)
              .orElseThrow(() -> new IllegalArgumentException("no composition found in bundle"));
      final String resp =
          contextEnrichmentServiceClient.getProvenanceResource(
              authorization, composition.getIdPart());
      final Provenance provenance = (Provenance) fhirParserService.parseFromJson(resp);
      final BundleEntryComponent entry =
          new BundleEntryComponent()
              .setResource(provenance)
              .setFullUrl(PROFILE_BASE_URL + provenance.getId());
      bundleOperationService.addEntry(bundle, entry);
    } catch (Exception e) {
      log.error("error while sending bundle to context enrichment service", e);
    }
  }
}
