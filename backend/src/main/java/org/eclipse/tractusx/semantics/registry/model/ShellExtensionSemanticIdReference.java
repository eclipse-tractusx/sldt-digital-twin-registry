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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Entity
@Getter
@Setter
@Table
@NoArgsConstructor
@AllArgsConstructor
@With
public class ShellExtensionSemanticIdReference {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   UUID id;

   ReferenceType type;


   @JsonManagedReference
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellExtensionSemanticIdReference")
   //@MappedCollection(idColumn = "fk_shell_extension_semantic_reference_id")
   Set<ShellExtensionSemanticIdReferenceKey> keys;

   @JsonManagedReference
   @OneToOne(cascade = CascadeType.ALL, mappedBy = "shellExtensionSemanticIdReference")
   //@Column(name = "fk_shell_extension_semantic_referred_id" )
   ShellExtensionSemanticIdReferenceParent referredSemanticId;

   @JsonBackReference
   @OneToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "fk_shell_ext_semantic_id")
   private ShellExtension shellExtension;

}
