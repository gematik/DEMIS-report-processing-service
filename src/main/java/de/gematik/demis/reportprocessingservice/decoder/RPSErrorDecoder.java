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

package de.gematik.demis.reportprocessingservice.decoder;

import de.gematik.demis.reportprocessingservice.exceptions.RestClientException;
import de.gematik.demis.reportprocessingservice.utils.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RPSErrorDecoder implements ErrorDecoder {

  private final String serviceName;
  private final ErrorCode errorCode5xx;
  private final ErrorCode errorCode4xx;

  public RPSErrorDecoder(String serviceName, ErrorCode errorCode4xx, ErrorCode errorCode5xx) {
    this.serviceName = serviceName;
    this.errorCode5xx = errorCode5xx;
    this.errorCode4xx = errorCode4xx;
  }

  @Override
  public RestClientException decode(String s, Response response) {
    String responseBody;
    try {
      responseBody =
          new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      responseBody = "Error reading response body";
    }
    log.error(
        "{} Call to {} with {} responded with error. response body {}",
        errorCode5xx.asString(),
        serviceName,
        s,
        responseBody);
    if (response.status() >= 400 && response.status() < 500) {
      return new RestClientException(errorCode4xx);
    }
    return new RestClientException(errorCode5xx);
  }
}
