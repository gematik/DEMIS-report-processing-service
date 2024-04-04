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

import static de.gematik.demis.reportprocessingservice.utils.ErrorCode.ERROR_IN_HLS_CALL;
import static de.gematik.demis.reportprocessingservice.utils.ErrorCode.ERROR_IN_HLS_CALL_5XX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.demis.reportprocessingservice.exceptions.InternalException;
import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RPSErrorDecoderTest {

  private final RPSErrorDecoder rpsErrorDecoder =
      new RPSErrorDecoder("someService", ERROR_IN_HLS_CALL, ERROR_IN_HLS_CALL_5XX);

  @Test
  void shouldReturnInternalExceptionWithError() throws IOException {
    String s = "500";
    Response responseMock = mock(Response.class);
    Response.Body bodyMock = mock(Response.Body.class);
    when(responseMock.body()).thenReturn(bodyMock);
    when(responseMock.status()).thenReturn(400);
    InputStream inputStreamMock = mock(InputStream.class);
    when(bodyMock.asInputStream()).thenReturn(inputStreamMock);
    when(inputStreamMock.readAllBytes()).thenReturn(("I'm a teapot!").getBytes());
    InternalException decode = rpsErrorDecoder.decode(s, responseMock);

    assertThat(decode.getCode()).isEqualTo(ERROR_IN_HLS_CALL);
  }

  @ParameterizedTest
  @ValueSource(ints = {399, 500})
  void shouldThrowExceptionForInputstream(int status) throws IOException {
    String s = "500";
    Response responseMock = mock(Response.class);
    Response.Body bodyMock = mock(Response.Body.class);
    when(responseMock.body()).thenReturn(bodyMock);
    when(responseMock.status()).thenReturn(status);
    InputStream inputStreamMock = mock(InputStream.class);
    when(bodyMock.asInputStream()).thenReturn(inputStreamMock);
    when(inputStreamMock.readAllBytes()).thenReturn(("I'm a teapot!").getBytes());
    InternalException decode = rpsErrorDecoder.decode(s, responseMock);

    assertThat(decode.getCode()).isEqualTo(ERROR_IN_HLS_CALL_5XX);
  }

  @Test
  void shouldHandleIOException() throws IOException {
    String s = "500";
    Response responseMock = mock(Response.class);
    Response.Body bodyMock = mock(Response.Body.class);
    when(responseMock.body()).thenReturn(bodyMock);
    when(responseMock.status()).thenReturn(500);
    InputStream inputStreamMock = mock(InputStream.class);
    when(bodyMock.asInputStream()).thenReturn(inputStreamMock);
    when(inputStreamMock.readAllBytes()).thenThrow(new IOException());
    InternalException decode = rpsErrorDecoder.decode(s, responseMock);

    assertThat(decode.getCode()).isEqualTo(ERROR_IN_HLS_CALL_5XX);
  }
}
