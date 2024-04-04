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

import de.gematik.demis.reportprocessingservice.decoder.RPSErrorDecoder;
import de.gematik.demis.reportprocessingservice.utils.ErrorCode;
import feign.Capability;
import feign.codec.ErrorDecoder;
import feign.micrometer.MicrometerCapability;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PDFGenerationClientConfiguration {

  @Bean(name = "pdfGenerationDecoder")
  public ErrorDecoder errorDecoder() {
    return new RPSErrorDecoder(
        "PDFGen-Service", ErrorCode.ERROR_IN_PDF_CALL, ErrorCode.ERROR_IN_PDF_CALL_5XX);
  }

  @Bean
  public Capability pdfCapability(final MeterRegistry registry) {
    return new MicrometerCapability(registry);
  }
}
