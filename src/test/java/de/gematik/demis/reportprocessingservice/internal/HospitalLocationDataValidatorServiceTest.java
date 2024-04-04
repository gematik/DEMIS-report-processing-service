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

package de.gematik.demis.reportprocessingservice.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.demis.notification.builder.demis.fhir.notification.builder.NotifierDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.ReportBedOccupancyDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.ReportBundleDataBuilder;
import de.gematik.demis.notification.builder.demis.fhir.notification.builder.reports.StatisticInformationBedOccupancyDataBuilder;
import de.gematik.demis.reportprocessingservice.connectors.hls.HospitalLocationConnectionService;
import de.gematik.demis.reportprocessingservice.exceptions.HospitalLocationValidationException;
import de.gematik.demis.reportprocessingservice.exceptions.InternalException;
import java.util.Collections;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HospitalLocationDataValidatorServiceTest {

  public static final String DEMIS_GATEWAY_VALUE = "demis-gateway";
  private static final String IK_NUMBER = "987654321";
  @InjectMocks private HospitalLocationDataValidatorService hospitalLocationDataValidatorService;
  @Mock private HospitalLocationConnectionService hospitalLocationConnectionServiceMock;

  @BeforeEach
  void setUp() {
    hospitalLocationDataValidatorService =
        new HospitalLocationDataValidatorService(
            "demis-test", hospitalLocationConnectionServiceMock);
  }

  @Test
  void shouldCallHospitallOcationConnectionService() {

    Bundle bundle = createBundleWithSpecificAddress();

    when(hospitalLocationConnectionServiceMock.getHospitalData(any()))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(
            () ->
                hospitalLocationDataValidatorService.validateLocationData(
                    IK_NUMBER, DEMIS_GATEWAY_VALUE, bundle))
        .isInstanceOf(HospitalLocationValidationException.class);

    verify(hospitalLocationConnectionServiceMock).getHospitalData(IK_NUMBER);
  }

  @Test
  void shouldThrowException() {
    Bundle bundle = createBundleWithSpecificAddress();

    when(hospitalLocationConnectionServiceMock.getHospitalData(any()))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(
            () ->
                hospitalLocationDataValidatorService.validateLocationData(
                    IK_NUMBER, DEMIS_GATEWAY_VALUE, bundle))
        .isInstanceOf(HospitalLocationValidationException.class)
        .hasMessage("Validation Exception (for id 987654 no InEK data found)");
  }

  @Test
  void shouldThrowExceptionForWrongEntryAtFirstEntry() {
    Bundle bundle = new Bundle();
    bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new Practitioner()));

    assertThatThrownBy(
            () ->
                hospitalLocationDataValidatorService.validateLocationData(
                    IK_NUMBER, DEMIS_GATEWAY_VALUE, bundle))
        .isInstanceOf(InternalException.class)
        .hasMessage("Internal Error (Error-ID-RPS-001)");
  }

  @Test
  void shouldReturnAndDoNothingIfGatewayIdEqualsProperty() {
    Bundle bundle = new Bundle();

    hospitalLocationDataValidatorService.validateLocationData(IK_NUMBER, "demis-test", bundle);
  }

  @Test
  void shouldReturnWhenDataIsCorrect() {
    Bundle bundle = createBundleWithSpecificAddress();

    HospitalLocationDTO location = HospitalLocationDTO.builder().locationId("987654").build();
    when(hospitalLocationConnectionServiceMock.getHospitalData(any()))
        .thenReturn(Collections.singletonList(location));

    hospitalLocationDataValidatorService.validateLocationData(
        IK_NUMBER, DEMIS_GATEWAY_VALUE, bundle);
  }

  private Bundle createBundleWithSpecificAddress() {
    ReportBundleDataBuilder reportBundleDataBuilder = new ReportBundleDataBuilder();
    PractitionerRole notifierRole = new NotifierDataBuilder().buildReportExampleNotifierData();
    reportBundleDataBuilder.setNotifierRole(notifierRole);
    QuestionnaireResponse statistic =
        new StatisticInformationBedOccupancyDataBuilder()
            .buildExampleStatisticInformationBedOccupancy();
    Composition composition =
        new ReportBedOccupancyDataBuilder()
            .setSubjectValue("987654")
            .buildExampleReportBedOccupancy(notifierRole, statistic);
    reportBundleDataBuilder.setReportBedOccupancy(composition);
    reportBundleDataBuilder.setStatisticInformationBedOccupancy(statistic);
    return reportBundleDataBuilder.buildExampleReportBundle();
  }
}
