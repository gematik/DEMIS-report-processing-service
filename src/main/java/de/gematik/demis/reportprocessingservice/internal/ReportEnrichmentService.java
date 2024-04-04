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

package de.gematik.demis.reportprocessingservice.internal;

import static java.lang.String.format;

import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Utils;
import de.gematik.demis.reportprocessingservice.utils.DateTimeService;
import de.gematik.demis.reportprocessingservice.utils.UUID5Generator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportEnrichmentService {

  public static final String EXTENSION_URL_RECEPTION_TIME_STAMP_TYPE =
      "https://demis.rki.de/fhir/StructureDefinition/ReceptionTimeStampType";

  public static final String RESPONSIBLE_HEALTH_DEPARTMENT_CODING_SYSTEM =
      "https://demis.rki.de/fhir/CodeSystem/ResponsibleDepartment";
  public static final String NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM =
      "https://demis.rki.de/fhir/NamingSystem/NotificationBundleId";
  private final DateTimeService dateTimeService;

  public void enrichReportBundle(Bundle bundle, String requestId) {
    log.debug("enrichment start");
    Optional<Composition> composition = getComposition(bundle);
    composition.ifPresent(this::setReceptionTimeStamp);
    setReceiver(bundle);

    String uuidFromRequestId;
    if (requestId != null && !requestId.isBlank()) {
      uuidFromRequestId = UUID5Generator.generateType5UUID(requestId).toString();
      log.debug(
          "Setting new UUID 5 bunde id from request id: {} -> {}", requestId, uuidFromRequestId);
    } else {
      log.error("FATAL: Cannot generate UUID 5 bundle id because request id is empty");
      uuidFromRequestId = Utils.generateUuidString();
      log.debug("Setting new random UUID bunde id: {}", uuidFromRequestId);
    }

    bundle.setIdentifier(
        new Identifier()
            .setSystem(NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM)
            .setValue(uuidFromRequestId));

    log.debug("enrichment end");
  }

  private void setReceiver(Bundle bundle) {
    Meta meta = bundle.getMeta();
    meta.addTag().setSystem(RESPONSIBLE_HEALTH_DEPARTMENT_CODING_SYSTEM).setCode("1.");
  }

  private Optional<Composition> getComposition(Bundle bundle) {
    Optional<Resource> first =
        bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(Composition.class::isInstance)
            .findFirst();
    return first.map(Composition.class::cast);
  }

  private void setReceptionTimeStamp(Composition composition) {
    DateTimeType timestamp = dateTimeService.getNow();
    Extension ex = new Extension();
    ex.setUrl(EXTENSION_URL_RECEPTION_TIME_STAMP_TYPE);
    ex.setValue(timestamp);
    composition.addExtension(ex);
    log.debug(format("added reception time %s", timestamp.getValue()));
  }
}
