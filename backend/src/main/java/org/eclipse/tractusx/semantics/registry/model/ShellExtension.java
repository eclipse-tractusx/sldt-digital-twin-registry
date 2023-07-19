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

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@JsonIdentityInfo(
      generator = ObjectIdGenerators.PropertyGenerator.class,
      property = "id")
public class ShellExtension {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   UUID id;

   String name;

   @Column(name = "shell_ext_value")
   String value;

   DataTypeXsd valueType;


   @JsonManagedReference
   @OneToOne(cascade = CascadeType.ALL, mappedBy = "shellExtension")
   //@Column(name = "fk_shell_ext_semantic_id")
   ShellExtensionSemanticIdReference semanticId;


   @JsonManagedReference
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellExtension")
   //@MappedCollection(idColumn = "fk_shell_ext_supplemental_id")
   Set<ShellExtensionSupplemSemanticIdReference> supplementalSemanticIds;

   @JsonManagedReference
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellExtension")
   //@MappedCollection(idColumn = "fk_shell_ext_refers_id")
   Set<ShellExtensionRefersToReference> refersTo;

   @JsonBackReference
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "fk_shell_id")
   private Shell shellId;

}
