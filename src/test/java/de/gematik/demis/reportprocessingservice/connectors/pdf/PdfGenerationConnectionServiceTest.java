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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.exceptions.RestClientException;
import de.gematik.demis.reportprocessingservice.utils.ErrorCode;
import java.util.Optional;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PdfGenerationConnectionServiceTest {

  private final String requestId = "111";
  private PdfGenerationConnectionService pdfGenerationConnectionService;
  @Mock private FhirParser fhirParserServiceMock;
  @Mock private PdfGenerationServiceClient pdfClientMock;

  @BeforeEach
  void init() {
    pdfGenerationConnectionService =
        new PdfGenerationConnectionService(fhirParserServiceMock, pdfClientMock);
  }

  @Test
  void shouldParseBundleAndMockGeneratePdf() {
    // given
    Bundle bundle = new Bundle();
    bundle.setId("bundleToSendId");

    byte[] bytes = "response".getBytes();

    String someBundleString = "SomeBundleString";
    when(fhirParserServiceMock.encodeToJson(any())).thenReturn(someBundleString);

    when(pdfClientMock.createBedOccupancyPdfFromJson(anyString()))
        .thenReturn(ResponseEntity.ok().body(bytes));

    // when
    Optional<Binary> response =
        pdfGenerationConnectionService.generateBedOccupancyReceipt(bundle, requestId);

    // then
    verify(pdfClientMock).createBedOccupancyPdfFromJson(someBundleString);
    assertThat(response).isNotEmpty();
    assertThat(response.get().getContent()).contains(bytes);
  }

  @Test
  void shouldCatchExceptionsFromFeignClientErrorDecoderAndReturnNull() {
    Bundle bundle = new Bundle();
    bundle.setId("bundleToSendId");

    byte[] bytes = "response".getBytes();

    String someBundleString = "SomeBundleString";
    when(fhirParserServiceMock.encodeToJson(any())).thenReturn(someBundleString);

    when(pdfClientMock.createBedOccupancyPdfFromJson(anyString()))
        .thenThrow(new RestClientException(ErrorCode.ERROR_IN_PDF_CALL));

    Optional<Binary> response =
        pdfGenerationConnectionService.generateBedOccupancyReceipt(bundle, requestId);

    verify(pdfClientMock).createBedOccupancyPdfFromJson(someBundleString);
    assertThat(response).isEmpty();
  }

  @Test
  void shouldNotCatchExceptionsNotFromFeignClientErrorDecoderAndReturnNull() {
    Bundle bundle = new Bundle();
    bundle.setId("bundleToSendId");

    byte[] bytes = "response".getBytes();

    String someBundleString = "SomeBundleString";
    when(fhirParserServiceMock.encodeToJson(any())).thenReturn(someBundleString);

    when(pdfClientMock.createBedOccupancyPdfFromJson(anyString()))
        .thenThrow(new RuntimeException());

    assertThatThrownBy(
            () -> pdfGenerationConnectionService.generateBedOccupancyReceipt(bundle, requestId))
        .isInstanceOf(Exception.class);
  }
}
