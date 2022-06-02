/********************************************************************************
 * Copyright (c) 2021-2022 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2021-2022 Contributors to the Eclipse Foundation
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
 ********************************************************************************/
package org.eclipse.tractusx.semantics.registry.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.semantics.aas.registry.model.IdentifierKeyValuePair;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This converter is required so that Spring is able to convert single query parameters to custom objects.
 */
@Component
public class IdentifierKeyValuePairConverter implements Converter<String, List<IdentifierKeyValuePair>> {

    private final ObjectMapper objectMapper;

    IdentifierKeyValuePairConverter(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public List<IdentifierKeyValuePair> convert(String source) {
        try {
            String processedSource = removeLineBreaks(source);
            if(processedSource.startsWith("{")) {
                return List.of(objectMapper.readValue(processedSource,IdentifierKeyValuePair.class));
            } else {
                return objectMapper.readValue(processedSource, new TypeReference<>() {
                });
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * SwaggerUI does weired encoding for user added items.
     * This method that SwaggerUI requests for lookups work.
     */
    private static String removeLineBreaks(String source){
        return StringEscapeUtils
                .unescapeJava(source).replace("\n", "").replace("\r", "")
                .replace("\"{", "{")
                .replace("}\"", "}");
    }
}
