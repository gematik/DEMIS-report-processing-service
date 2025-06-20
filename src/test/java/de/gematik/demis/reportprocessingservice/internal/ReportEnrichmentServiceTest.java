package de.gematik.demis.reportprocessingservice.internal;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.gematik.demis.reportprocessingservice.utils.BundleOperationService;
import de.gematik.demis.reportprocessingservice.utils.DateTimeService;
import de.gematik.demis.reportprocessingservice.utils.UUID5Generator;
import java.util.Date;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Type;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class ReportEnrichmentServiceTest {

  public static final String REQUEST_ID = "request-id";
  private static final String BUNDLE_IDENTIFIER = "bundle-id";
  private static Date date;
  private ListAppender<ILoggingEvent> listAppender;
  @InjectMocks private ReportEnrichmentService reportEnrichmentService;
  @Mock private BundleOperationService bundleOperationServiceMock;
  @Mock private DateTimeService dateTimeServiceMock;

  ReportEnrichmentServiceTest() {}

  @BeforeAll
  static void init() {
    date = new Date();
  }

  @BeforeEach
  void initTestLogger() {
    final Logger log = (Logger) LoggerFactory.getLogger(ReportEnrichmentService.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    log.addAppender(listAppender);
  }

  @Test
  void shouldAddExtension() {
    when(dateTimeServiceMock.getNow()).thenReturn(new DateTimeType(date));

    Composition composition = mock(Composition.class);

    Bundle.BundleEntryComponent bundleEntryComponentComposition = new Bundle.BundleEntryComponent();
    bundleEntryComponentComposition.setResource(composition);

    Bundle bundle = new Bundle();
    bundle.addEntry(bundleEntryComponentComposition);

    when(bundleOperationServiceMock.getComposition(bundle)).thenReturn(Optional.of(composition));

    reportEnrichmentService.enrichReportBundle(bundle, REQUEST_ID);

    verify(composition).addExtension(any());
  }

  @Test
  void shouldAddTimestampWithUrlToCompositionInBundle() {
    when(dateTimeServiceMock.getNow()).thenReturn(new DateTimeType(date));

    Composition composition = new Composition();

    Bundle.BundleEntryComponent bundleEntryComponentComposition = new Bundle.BundleEntryComponent();
    bundleEntryComponentComposition.setResource(composition);

    Bundle bundle = new Bundle();
    bundle.addEntry(bundleEntryComponentComposition);

    when(bundleOperationServiceMock.getComposition(bundle)).thenReturn(Optional.of(composition));

    reportEnrichmentService.enrichReportBundle(bundle, REQUEST_ID);

    Type value =
        composition
            .getExtensionByUrl(
                "https://demis.rki.de/fhir/StructureDefinition/ReceptionTimeStampType")
            .getValue();
    assertThat(value).isExactlyInstanceOf(DateTimeType.class);
    DateTimeType dateTimeType = (DateTimeType) value;
    assertThat(dateTimeType.getValue()).isEqualTo(date);
  }

  @Test
  void shouldAddMetaTagWithReceiverInfo() {
    Bundle bundle = new Bundle();
    reportEnrichmentService.enrichReportBundle(bundle, REQUEST_ID);

    assertThat(bundle.getMeta().getTag()).hasSize(1);
    assertThat(bundle.getMeta().getTag().get(0).getCode()).isEqualTo("1.");
    assertThat(bundle.getMeta().getTag().get(0).getSystem())
        .isEqualTo("https://demis.rki.de/fhir/CodeSystem/ResponsibleDepartment");
  }

  @Test
  void whenRequestIdGivenCheckBundleIdentifierIsOverwritten() {
    Bundle bundle = new Bundle();
    bundle.setIdentifier(
        new Identifier()
            .setSystem(ReportEnrichmentService.NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM)
            .setValue(BUNDLE_IDENTIFIER));
    reportEnrichmentService.enrichReportBundle(bundle, REQUEST_ID);

    String uuidFromRequestId = UUID5Generator.generateType5UUID(REQUEST_ID).toString();
    assertThat(bundle.getIdentifier().getValue()).isEqualTo(uuidFromRequestId);
    assertThat(bundle.getIdentifier().getSystem())
        .isEqualTo(ReportEnrichmentService.NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM);
  }

  @Test
  void whenNoRequestIdGivenCheckBundleIdentifierIsOverwrittenAndThereIsAnErrorMessage() {
    Bundle bundle = new Bundle();
    bundle.setIdentifier(
        new Identifier()
            .setSystem(ReportEnrichmentService.NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM)
            .setValue(BUNDLE_IDENTIFIER));
    reportEnrichmentService.enrichReportBundle(bundle, null);

    assertThat(bundle.getIdentifier().getValue()).isNotEqualTo(BUNDLE_IDENTIFIER);
    assertThat(bundle.getIdentifier().getSystem())
        .isEqualTo(ReportEnrichmentService.NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM);

    Optional<ILoggingEvent> firstErrorMessage = getFirstErrorEvent();
    assertThat(firstErrorMessage).isNotEmpty();
    assertThat(firstErrorMessage.get().getFormattedMessage())
        .contains("FATAL: Cannot generate UUID 5 bundle id because request id is empty");
  }

  @Test
  void whenRequestIdIsASpaceGivenCheckBundleIdentifierIsOverwrittenAndThereIsAnErrorMessage() {
    Bundle bundle = new Bundle();
    bundle.setIdentifier(
        new Identifier()
            .setSystem(ReportEnrichmentService.NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM)
            .setValue(BUNDLE_IDENTIFIER));
    reportEnrichmentService.enrichReportBundle(bundle, " ");

    assertThat(bundle.getIdentifier().getValue()).isNotEqualTo(BUNDLE_IDENTIFIER);
    assertThat(bundle.getIdentifier().getSystem())
        .isEqualTo(ReportEnrichmentService.NOTIFICATION_BUNDLE_IDENTIFIER_SYSTEM);

    Optional<ILoggingEvent> firstErrorMessage = getFirstErrorEvent();
    assertThat(firstErrorMessage).isNotEmpty();
    assertThat(firstErrorMessage.get().getFormattedMessage())
        .contains("FATAL: Cannot generate UUID 5 bundle id because request id is empty");
  }

  private Optional<ILoggingEvent> getFirstErrorEvent() {
    for (ILoggingEvent event : listAppender.list) {
      if (Level.ERROR.equals(event.getLevel())) {
        return Optional.of(event);
      }
    }

    return Optional.empty();
  }
}
