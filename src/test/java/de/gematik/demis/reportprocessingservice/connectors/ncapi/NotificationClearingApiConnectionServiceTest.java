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

package de.gematik.demis.reportprocessingservice.connectors.ncapi;

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
class NotificationClearingApiConnectionServiceTest {

  private NotificationClearingApiConnectionService notificationClearingApiConnectionService;

  @Mock private NotificationClearingApiClient notificationClearingApiClientMock;
  @Mock private FhirParser fhirParserServiceMock;

  @BeforeEach
  void init() {
    notificationClearingApiConnectionService =
        new NotificationClearingApiConnectionService(
            "theapikey", notificationClearingApiClientMock, fhirParserServiceMock);
  }

  @Test
  void shouldParseBundleAndPutBundleInTransactionBundleAndSendTransactionBundle() {

    Bundle bundle = new Bundle();
    bundle.setId("bundleToSendId");

    String someBundleString = "SomeBundleString";
    when(fhirParserServiceMock.encodeToJson(any())).thenReturn(someBundleString);

    when(notificationClearingApiClientMock.sendNotificationToNotificationClearingAPI(
            anyString(), anyString()))
        .thenReturn(ResponseEntity.ok().build());

    notificationClearingApiConnectionService.sendReportBundleToNCAPI(bundle);

    verify(notificationClearingApiClientMock)
        .sendNotificationToNotificationClearingAPI("Bearer theapikey", someBundleString);
  }

  @Test
  void shouldThrowExceptionForNotOkResponseEntity() {

    Bundle bundle = new Bundle();
    bundle.setId("bundleToSendId");

    String someBundleString = "SomeBundleString";
    when(fhirParserServiceMock.encodeToJson(any())).thenReturn(someBundleString);

    when(notificationClearingApiClientMock.sendNotificationToNotificationClearingAPI(
            anyString(), anyString()))
        .thenReturn(ResponseEntity.badRequest().build());

    assertThatThrownBy(
            () -> notificationClearingApiConnectionService.sendReportBundleToNCAPI(bundle))
        .isInstanceOf(InternalException.class)
        .hasMessage("Internal Error (Error-ID-RPS-003)");
  }
}
