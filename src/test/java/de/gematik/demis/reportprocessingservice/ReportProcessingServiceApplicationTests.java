package de.gematik.demis.reportprocessingservice;

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

import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.connectors.validation.ValidationServiceConnectionService;
import de.gematik.demis.reportprocessingservice.processor.ReportProcessingController;
import de.gematik.demis.reportprocessingservice.processor.ReportProcessingService;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = {SpringDocConfiguration.class})
class ReportProcessingServiceApplicationTests {

  @Autowired private ReportProcessingController reportProcessingController;

  @Autowired private ReportProcessingService reportProcessingService;

  @Autowired private ValidationServiceConnectionService validationServiceConnectionService;

  @Autowired private FhirParser fhirParserService;

  /**
   * just a quick sanity check to see if all controllers are loaded. please add any controller you
   * add to this test
   */
  @Test
  void controllerContextLoads() {
    assertThat(reportProcessingController).isNotNull();
  }

  /**
   * just a quick sanity check to see if all services are loaded. please add any services you add to
   * this test
   */
  @Test
  void serviceContextLoads() {
    assertThat(reportProcessingService).isNotNull();
    assertThat(validationServiceConnectionService).isNotNull();
    assertThat(fhirParserService).isNotNull();
  }
}
