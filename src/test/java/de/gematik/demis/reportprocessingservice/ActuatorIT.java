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

package de.gematik.demis.reportprocessingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {SpringDocConfiguration.class})
class ActuatorIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldAllowAccessToHealthUrls() throws Exception {
    MvcResult healthMvcResult =
        mockMvc.perform(get("/actuator/health")).andExpect(status().is2xxSuccessful()).andReturn();

    assertThat(healthMvcResult.getResponse().getStatus()).isEqualTo(200);
    assertThat(healthMvcResult.getResponse().getContentAsString())
        .contains("{\"status\":\"UP\",\"groups\":[\"liveness\",\"readiness\"]}");

    MvcResult readinessMvcResult =
        mockMvc
            .perform(get("/actuator/health/readiness"))
            .andExpect(status().is2xxSuccessful())
            .andReturn();

    assertThat(readinessMvcResult.getResponse().getStatus()).isEqualTo(200);
    assertThat(readinessMvcResult.getResponse().getContentAsString())
        .contains("{\"status\":\"UP\"}");

    MvcResult livenessMvcResult =
        mockMvc
            .perform(get("/actuator/health/liveness"))
            .andExpect(status().is2xxSuccessful())
            .andReturn();

    assertThat(livenessMvcResult.getResponse().getStatus()).isEqualTo(200);
    assertThat(livenessMvcResult.getResponse().getContentAsString())
        .contains("{\"status\":\"UP\"}");
  }
}
