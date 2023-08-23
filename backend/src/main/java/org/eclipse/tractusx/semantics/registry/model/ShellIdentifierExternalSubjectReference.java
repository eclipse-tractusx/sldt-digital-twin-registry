/********************************************************************************
 * Copyright (c) 2023 Robert Bosch Manufacturing Solutions GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity
@Getter
@Setter
@Table
@NoArgsConstructor
@AllArgsConstructor
@With
@FieldNameConstants
public class ShellIdentifierExternalSubjectReference {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   UUID id;
   @Column
   ReferenceType type;
   @JsonManagedReference
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellIdentifierExternalSubjectReference")
   private Set<ShellIdentifierExternalSubjectReferenceKey> keys;

   @JsonBackReference
   @OneToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "fk_shell_identifier_external_subject_id")
   private ShellIdentifier shellIdentifier;
}
