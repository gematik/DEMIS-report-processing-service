package de.gematik.demis.reportprocessingservice.testobjects;

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

import static de.gematik.demis.reportprocessingservice.testobjects.TestUtils.getJsonParser;

import ca.uhn.fhir.util.TestUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.r4.model.Bundle;

public class TestObjects {

  public static final String PROVENANCE_RESOURCE = "/entries/provenanceResource.json";

  public static String standardContent() throws IOException {
    return new String(
        TestObjects.class
            .getClassLoader()
            .getResourceAsStream("example-reports/BedOccupancyExample.json")
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  public static String standardReturnContent() throws IOException {
    return new String(
        TestObjects.class
            .getClassLoader()
            .getResourceAsStream("example-reports/BedOccupancyExampleReturn.json")
            .readAllBytes(),
        StandardCharsets.UTF_8);
  }

  public static Bundle getBundle(final String resourceName) {
    return getJsonParser().parseResource(Bundle.class, readResourceAsString(resourceName));
  }

  public static String readResourceAsString(final String resourceName) {
    return new String(readResourceBytes(resourceName), StandardCharsets.UTF_8);
  }

  public static byte[] readResourceBytes(final String resourceName) {
    try (final InputStream is = readResource(resourceName)) {
      return is.readAllBytes();
    } catch (final IOException e) {
      throw new UncheckedIOException("error reading classpath resource " + resourceName, e);
    }
  }

  public static InputStream readResource(final String resourceName) {
    final InputStream is = TestUtil.class.getResourceAsStream(resourceName);
    if (is == null) {
      throw new IllegalStateException("missing resource file " + resourceName);
    }
    return is;
  }
}
