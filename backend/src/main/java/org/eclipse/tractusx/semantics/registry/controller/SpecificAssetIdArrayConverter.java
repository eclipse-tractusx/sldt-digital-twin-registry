/********************************************************************************
 * Copyright (c) 2021-2023 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2021-2023 Contributors to the Eclipse Foundation
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

import org.eclipse.tractusx.semantics.aas.registry.model.SpecificAssetId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * This converter is required so that Spring is able to convert array-style query parameters to custom objects.
 */
@Component
@RequiredArgsConstructor
public class SpecificAssetIdArrayConverter implements Converter<String[], List<SpecificAssetId>>{

    private final SpecificAssetIdConverter singleConverter;

    @Override
    public List<SpecificAssetId> convert(String[] source) {
        List<SpecificAssetId> result = new ArrayList<>(source.length);
        for (int count = 0; count < source.length; count++) {
            result.addAll(singleConverter.convert(source[count]));
        }
        return result;
    }
}
