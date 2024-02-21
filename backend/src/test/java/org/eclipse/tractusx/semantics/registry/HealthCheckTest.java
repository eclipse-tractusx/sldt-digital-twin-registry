/*******************************************************************************
 * Copyright (c) 2021 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.eclipse.tractusx.semantics.registry;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class HealthCheckTest {
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String LIVENESS_ENDPOINT = HEALTH_ENDPOINT + "/liveness";
    private static final String READINESS_ENDPOINT = HEALTH_ENDPOINT + "/readiness";

    @Autowired
    private MockMvc mvc;

    @Test
    public void testHealthEndpoint() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(HEALTH_ENDPOINT))
            .andExpect(jsonPath("$.status", is("UP")))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testLivenessEndpoint() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(LIVENESS_ENDPOINT))
            .andExpect(jsonPath("$.status", is("UP")))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testReadinessEndpoint() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(READINESS_ENDPOINT))
            .andExpect(jsonPath("$.status", is("UP")))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testInfoEndpoint() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/actuator/info"))
                .andExpect(jsonPath("$.git.commit.id", notNullValue()))
                .andExpect(status().is2xxSuccessful());
    }
}
