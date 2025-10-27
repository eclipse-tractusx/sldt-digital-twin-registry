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

package org.eclipse.tractusx.semantics.registry.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class SubmodelEndpoint {

   @Id
   private UUID id = UuidCreator.getTimeOrderedEpoch();
   @Column
   private String interfaceName;
   @Column
   private String endpointAddress;
   @Column
   private String endpointProtocol;
   @Column
   private String endpointProtocolVersion;
   @Column
   private String subProtocol;
   @Column
   private String subProtocolBody;
   @Column
   private String subProtocolBodyEncoding;

   @JsonManagedReference
   @JsonIgnore
   @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true,mappedBy = "submodelEndpoint")
   private Set<SubmodelSecurityAttribute> submodelSecurityAttribute=new HashSet<>();

   @JsonBackReference
   @ManyToOne( fetch = FetchType.LAZY, optional = false,cascade = {CascadeType.MERGE}  )
   @JoinColumn( name = "fk_submodel_id" )
   private Submodel submodel;
}
