package de.gematik.demis.reportprocessingservice.connectors.fhirstorage;

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

import static de.gematik.demis.reportprocessingservice.utils.ErrorCode.ERROR_IN_FSW_CALL;
import static java.lang.String.format;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.exceptions.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FhirStorageWriterConnectionService {

  private final FhirStorageWriterClient fhirStorageWriterClient;
  private final FhirParser fhirParserService;

  public FhirStorageWriterConnectionService(
      FhirStorageWriterClient fhirStorageWriterClient, FhirParser fhirParserService) {
    this.fhirStorageWriterClient = fhirStorageWriterClient;
    this.fhirParserService = fhirParserService;
  }

  public void sendReportBundleToFhirStorage(Bundle bundle) {

    Bundle transactionBundle = createTransactionBundleWithReceviedBundleAsEntry(bundle);

    String bundleAsJson = fhirParserService.encodeToJson(transactionBundle);
    log.info("sending bundle {} to fhir-storage-writer", bundle.getId());
    ResponseEntity<String> stringResponseEntity =
        fhirStorageWriterClient.sendNotificationToFhirStorageWriter(bundleAsJson);
    log.info(
        format(
            "notification send to fhir-storage-writer, return code is %d",
            stringResponseEntity.getStatusCode().value()));
    if (!stringResponseEntity.getStatusCode().is2xxSuccessful()) {
      log.error("sending to fhir-storage-writer ended with an error");
      throw new InternalException(ERROR_IN_FSW_CALL);
    }
  }

  private Bundle createTransactionBundleWithReceviedBundleAsEntry(Resource resource) {
    Bundle transactionBundle = new Bundle();
    transactionBundle.setType(Bundle.BundleType.TRANSACTION);
    transactionBundle
        .addEntry()
        .setFullUrl(IdType.newRandomUuid().getValue())
        .setResource(resource)
        .getRequest()
        .setUrl("Bundle")
        .setMethod(Bundle.HTTPVerb.POST);
    return transactionBundle;
  }
}
