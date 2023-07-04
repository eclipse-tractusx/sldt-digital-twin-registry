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
package org.eclipse.tractusx.semantics.registry.model;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;

import lombok.Value;
import lombok.With;

@Value
@With
public class Submodel {
    @Id
    UUID id;

    String idExternal;
    String idShort;
    String semanticId;

    @MappedCollection(idColumn = "fk_submodel_id")
    Set<SubmodelDescription> descriptions;

    @MappedCollection(idColumn = "fk_submodel_id")
    Set<SubmodelEndpoint> endpoints;

    @Column( "fk_shell_id")
    UUID shellId;

    @MappedCollection(idColumn = "fk_submodel_id")
    Set<SubmodelDisplayName> displayNames;
}
