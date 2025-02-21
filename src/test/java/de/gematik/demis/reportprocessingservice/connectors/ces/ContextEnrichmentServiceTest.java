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

package de.gematik.demis.reportprocessingservice.connectors.ces;

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

import static de.gematik.demis.reportprocessingservice.objects.TestObjects.bundles;
import static de.gematik.demis.reportprocessingservice.testobjects.TestObjects.PROVENANCE_RESOURCE;
import static de.gematik.demis.reportprocessingservice.testobjects.TestUtils.getJsonParser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.reportprocessingservice.testobjects.TestObjects;
import de.gematik.demis.reportprocessingservice.utils.BundleOperationService;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Provenance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContextEnrichmentServiceTest {

  @Captor private ArgumentCaptor<BundleEntryComponent> entityComponentCaptor;
  @Mock private ContextEnrichmentServiceClient contextEnrichmentServiceClient;
  @Mock private FhirParser fhirParser;
  @Mock BundleOperationService bundleOperationService;
  ContextEnrichmentService underTest;

  private Bundle bundle;
  private final String TOKEN = "SomeToken";

  @BeforeEach
  void setUp() {
    bundle = bundles().minimalBundle();
    underTest =
        new ContextEnrichmentService(
            contextEnrichmentServiceClient, fhirParser, bundleOperationService);
  }

  @Test
  @DisplayName(
      "Test that if enrichment flag is enabled and authorization is null, contextEnrichmentServiceClient and bundleOperationService don't got called")
  void testShouldNotInteractWithClientIfAuthorizationIsNotSet() {
    underTest.enrichBundleWithContextInformation(bundle, null);
    verifyNoInteractions(bundleOperationService);
    verifyNoInteractions(contextEnrichmentServiceClient);
  }

  @Test
  @DisplayName(
      "Test that if enrichment flag is enabled, the bundle is not modified if bundleOperationService can not find composition and clientEnrichmentServiceClient does not get called")
  void testBundleDoesNotGetModifiedIfBundleOperationServiceCanNotFindComposition() {
    when(bundleOperationService.getComposition(bundle)).thenReturn(Optional.empty());
    underTest.enrichBundleWithContextInformation(bundle, TOKEN);
    verifyNoInteractions(contextEnrichmentServiceClient);
  }

  @Test
  @DisplayName(
      "Test that if enrichment flag is enabled, fhirParser is not called if client throws an error")
  void testFhirParserDoesNotGetCalledIfClientError() {
    when(bundleOperationService.getComposition(bundle)).thenReturn(getComposition(bundle));
    when(contextEnrichmentServiceClient.getProvenanceResource(any(), any()))
        .thenThrow(new RuntimeException("Some error"));
    underTest.enrichBundleWithContextInformation(bundle, TOKEN);
    verify(fhirParser, times(0)).parseFromJson(anyString());
  }

  @Test
  @DisplayName(
      "Test that if enrichment flag is enabled, the bundle have not been changed if client throws an error")
  void testIfTheSameBundleIsReturnedAsFallbackWhenClientError() {
    String bundleString = getJsonParser().encodeToString(bundle);
    when(bundleOperationService.getComposition(bundle)).thenReturn(getComposition(bundle));
    when(contextEnrichmentServiceClient.getProvenanceResource(any(), any()))
        .thenThrow(new RuntimeException("Some error"));
    underTest.enrichBundleWithContextInformation(bundle, TOKEN);
    assertThat(getJsonParser().encodeToString(bundle)).isEqualTo(bundleString);
  }

  @Test
  @DisplayName(
      "Test that if enrichment flag is enabled, the bundle have not been changed if the contextEnrichmentServiceClient returns invalid data")
  void testIfTheSameBundleIsReturnedAsFallbackWhenBundleHaveNoProvenance() {
    String bundleString = getJsonParser().encodeToString(bundle);
    when(bundleOperationService.getComposition(bundle)).thenReturn(getComposition(bundle));
    when(contextEnrichmentServiceClient.getProvenanceResource(any(), any()))
        .thenReturn("changedBundle");
    when(fhirParser.parseFromJson(anyString())).thenReturn(bundle);

    underTest.enrichBundleWithContextInformation(bundle, TOKEN);

    assertThat(getJsonParser().encodeToString(bundle)).isEqualTo(bundleString);
  }

  @Test
  @DisplayName("Test that if enrichment flag is enabled, the bundle enriched correctly")
  void testProvenanceGotAppendCorrectly() {
    String response = "changedBundle";
    Provenance provenance =
        getJsonParser()
            .parseResource(Provenance.class, TestObjects.readResourceAsString(PROVENANCE_RESOURCE));
    when(bundleOperationService.getComposition(bundle)).thenReturn(getComposition(bundle));
    when(contextEnrichmentServiceClient.getProvenanceResource(
            TOKEN, getComposition(bundle).orElseThrow().getIdPart()))
        .thenReturn(response);
    when(fhirParser.parseFromJson(eq(response))).thenReturn(provenance);

    underTest.enrichBundleWithContextInformation(bundle, TOKEN);
    verify(bundleOperationService).addEntry(eq(bundle), entityComponentCaptor.capture());
    assertThat(entityComponentCaptor.getValue().getResource())
        .usingRecursiveComparison()
        .isEqualTo(provenance);
  }

  private Optional<Composition> getComposition(Bundle bundle) {
    return bundle.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(Composition.class::isInstance)
        .map(Composition.class::cast)
        .findFirst();
  }
}
