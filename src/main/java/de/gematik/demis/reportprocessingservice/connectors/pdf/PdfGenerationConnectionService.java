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

package de.gematik.demis.reportprocessingservice.connectors.pdf;

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

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.exceptions.RestClientException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PdfGenerationConnectionService {

  private final FhirParser fhirParserService;

  private final PdfGenerationServiceClient pdfGenerationServiceClient;

  public PdfGenerationConnectionService(
      FhirParser fhirParserService, PdfGenerationServiceClient pdfGenerationServiceClient) {
    this.fhirParserService = fhirParserService;
    this.pdfGenerationServiceClient = pdfGenerationServiceClient;
  }

  public Optional<Binary> generateBedOccupancyReceipt(Bundle bundle, String requestId) {
    String bundleAsJsonString = fhirParserService.encodeToJson(bundle);
    try {
      log.info("sending bundle {} to pdfgen service for request {}", bundle.getId(), requestId);
      ResponseEntity<byte[]> responseEntity =
          pdfGenerationServiceClient.createBedOccupancyPdfFromJson(bundleAsJsonString);

      Binary binary = new Binary();
      binary.setContent(responseEntity.getBody());
      log.info("pdfgen service responded with pdf");
      return Optional.of(binary);
    } catch (RestClientException pdfException) {
      log.error("No PDF was generated for request {}", requestId, pdfException);
    }
    return Optional.empty();
  }
}
