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

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@EntityListeners( AuditingEntityListener.class)
public class Submodel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private UUID id;

    @Column
    private String idExternal;
    @Column
    private String idShort;

    @JsonManagedReference
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL,orphanRemoval=true, mappedBy = "submodel")
    SubmodelSemanticIdReference semanticId;

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval=true, mappedBy = "submodel")
    private Set<SubmodelDescription> descriptions= new HashSet<>();

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true,mappedBy = "submodel")
    private Set<SubmodelEndpoint> endpoints= new HashSet<>();

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_shell_id")
    private Shell shellId;

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true,mappedBy = "submodel")
    private Set<SubmodelDisplayName> displayNames= new HashSet<>();

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true,mappedBy = "submodel")
    Set<SubmodelSupplemSemanticIdReference> submodelSupplemSemanticIds;

    public void setDisplayNames(Set<SubmodelDisplayName> displayNames) {
        if(displayNames==null) {displayNames = new HashSet<>();}
        this.displayNames = displayNames;
        for(SubmodelDisplayName s : displayNames) {
            s.setSubmodel(this);
        }
    }

    public void setDescriptions(Set<SubmodelDescription> descriptions) {
        if(descriptions==null) {descriptions = new HashSet<>();}
        this.descriptions = descriptions;
        for(SubmodelDescription s : descriptions) {
            s.setSubmodel(this);
        }
    }

    public void setEndpoints(Set<SubmodelEndpoint> endpoints) {
        if(endpoints==null) {endpoints = new HashSet<>();}
        this.endpoints = endpoints;
        for(SubmodelEndpoint s : endpoints) {
            s.setSubmodel(this);
        }
    }
}
