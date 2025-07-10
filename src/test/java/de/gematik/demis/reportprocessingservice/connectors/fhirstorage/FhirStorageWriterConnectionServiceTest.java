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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.exceptions.InternalException;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FhirStorageWriterConnectionServiceTest {

  private FhirStorageWriterConnectionService fhirStorageWriterConnectionService;

  @Mock private FhirStorageWriterClient fhirStorageWriterClientMock;

  @Mock private FhirParser fhirParserServiceMock;

  @BeforeEach
  void init() {
    fhirStorageWriterConnectionService =
        new FhirStorageWriterConnectionService(fhirStorageWriterClientMock, fhirParserServiceMock);
  }

  @Test
  void shouldParseBundleAndPutBundleInTransactionBundleAndSendTransactionBundle() {

    Bundle bundle = new Bundle();
    bundle.setId("bundleToSendId");

    String someBundleString = "SomeBundleString";
    when(fhirParserServiceMock.encodeToJson(any())).thenReturn(someBundleString);

    when(fhirStorageWriterClientMock.sendNotificationToFhirStorageWriter(anyString()))
        .thenReturn(ResponseEntity.ok().build());

    fhirStorageWriterConnectionService.sendReportBundleToFhirStorage(bundle);

    verify(fhirStorageWriterClientMock).sendNotificationToFhirStorageWriter(someBundleString);
  }

  @Test
  void shouldThrowExceptionForNotOkResponseEntity() {

    Bundle bundle = new Bundle();
    bundle.setId("bundleToSendId");

    String someBundleString = "SomeBundleString";
    when(fhirParserServiceMock.encodeToJson(any())).thenReturn(someBundleString);

    when(fhirStorageWriterClientMock.sendNotificationToFhirStorageWriter(anyString()))
        .thenReturn(ResponseEntity.badRequest().build());

    assertThatThrownBy(
            () -> fhirStorageWriterConnectionService.sendReportBundleToFhirStorage(bundle))
        .isInstanceOf(InternalException.class)
        .hasMessage("Internal Error (Error-ID-RPS-003)");
  }
}
