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

package de.gematik.demis.reportprocessingservice.objects;

import static de.gematik.demis.reportprocessingservice.processor.ReportProcessingService.HTTPS_DEMIS_RKI_DE_FHIR_STRUCTURE_DEFINITION_REPORT_BUNDLE;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;

public class TestBundles {

  public Bundle minimalBundle() {
    Bundle bundle = new Bundle();
    final Meta meta = new Meta();
    meta.getProfile()
        .add(new CanonicalType(HTTPS_DEMIS_RKI_DE_FHIR_STRUCTURE_DEFINITION_REPORT_BUNDLE));
    bundle.setMeta(meta);
    bundle.addEntry(
        new Bundle.BundleEntryComponent()
            .setResource(
                new Composition()
                    .setIdentifier(new Identifier().setValue("composition-identifier"))));
    return bundle;
  }
}
