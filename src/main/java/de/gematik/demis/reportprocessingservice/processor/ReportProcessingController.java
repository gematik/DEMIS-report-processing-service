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

package de.gematik.demis.reportprocessingservice.processor;

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

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import de.gematik.demis.fhirparserlibrary.ParsingException;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
@Validated
@ConditionalOnProperty(name = "demis.idp.validate-jwt", havingValue = "false")
public class ReportProcessingController {

  static final String X_REQUEST_ID_HEADER = "X-Request-ID";
  static final String IK_NUMBER_HEADER = "IK-Number";
  static final String AZP_HEADER = "azp";

  private final ReportProcessingService reportProcessingService;

  @PostMapping(
      path = "/$process-report",
      consumes = {
        APPLICATION_JSON_VALUE,
        APPLICATION_XML_VALUE,
        "application/json+fhir",
        "application/fhir+json"
      },
      produces = {
        APPLICATION_JSON_VALUE,
        APPLICATION_XML_VALUE,
        "application/json+fhir",
        "application/fhir+json"
      })
  public ResponseEntity<String> processReport(
      @RequestBody @NotBlank String content,
      @RequestHeader(CONTENT_TYPE) MediaType mediaType,
      @RequestHeader(name = ACCEPT, required = false) MediaType accept,
      @RequestHeader(value = X_REQUEST_ID_HEADER, required = false) String requestId,
      @RequestHeader(value = IK_NUMBER_HEADER, required = false) String ikNumber,
      @RequestHeader(value = AZP_HEADER) String azp,
      @RequestHeader(value = AUTHORIZATION, required = false) String authorization)
      throws ParsingException {
    return reportProcessingService.process(
        content, mediaType, requestId, accept, ikNumber, azp, authorization);
  }
}
