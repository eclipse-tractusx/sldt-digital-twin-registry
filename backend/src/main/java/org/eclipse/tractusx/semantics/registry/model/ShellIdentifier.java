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
package org.eclipse.tractusx.semantics.registry.model;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table
@NoArgsConstructor
@AllArgsConstructor
@With
@JsonIdentityInfo(
      generator = ObjectIdGenerators.PropertyGenerator.class,
      property = "id")
public class ShellIdentifier {

    public static final String GLOBAL_ASSET_ID_KEY = "globalAssetId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private UUID id;
    @Column(name = "namespace")
    private String key;
    @Column(name = "identifier")
    private String value;
    @Column(name = "external_subject_id")
    private String externalSubjectId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_shell_id")
    private Shell shellId;

    @Column("fk_shell_identifier_external_subject_id")
    ShellIdentifierExternalSubjectReference externalSubjectId;

    @Column( "fk_shell_identifier_semantic_id")
    ShellIdentifierSemanticReference semanticId;

    @MappedCollection(idColumn = "fk_shell_identifier_supplem_semantic_id")
    Set<ShellIdentifierSupplemSemanticReference> supplementalSemanticIds;

}
