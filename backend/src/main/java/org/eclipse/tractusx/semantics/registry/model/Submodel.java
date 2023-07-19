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

import java.time.Instant;
import java.util.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    //@Column("fk_submodel_id")
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
    //@MappedCollection(idColumn = "fk_submodel_id")
    private Set<SubmodelDisplayName> displayNames= new HashSet<>();

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true,mappedBy = "submodel")
    //@MappedCollection(idColumn = "fk_submodel_id")
    Set<SubmodelExtension> submodelExtensions= new HashSet<>();

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true,mappedBy = "submodel")
    //@MappedCollection(idColumn = "fk_submodel_id")
    Set<SubmodelSupplemSemanticIdReference> submodelSupplemSemanticIds;

    public void setDisplayNames(Set<SubmodelDisplayName> displayNames) {
        if(displayNames==null) {displayNames = new HashSet<>();}
        this.displayNames = displayNames;
        for(SubmodelDisplayName s : displayNames) {
            s.setSubmodel(this);
        }
    }

    public void setSubmodelExtensions(Set<SubmodelExtension> submodelExtensions) {
        if(submodelExtensions==null) {submodelExtensions = new HashSet<>();}
        this.submodelExtensions = submodelExtensions;
        for(SubmodelExtension s : submodelExtensions) {
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
