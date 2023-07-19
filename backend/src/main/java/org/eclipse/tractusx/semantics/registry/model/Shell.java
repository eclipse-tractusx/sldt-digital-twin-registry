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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
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
@EntityListeners( AuditingEntityListener.class)
@JsonIdentityInfo(
      generator = ObjectIdGenerators.PropertyGenerator.class,
      property = "id")
public class Shell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "id_external", nullable = false)
    private String idExternal;

    @Column
    private String idShort;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellId")
    private Set<ShellIdentifier> identifiers = new HashSet<>();

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellId")
    private Set<ShellDescription> descriptions= new HashSet<>();

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellId")
    private Set<Submodel> submodels = new HashSet<>();

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellId")
    Set<ShellDisplayName> displayNames= new HashSet<>();

    @Column
    @CreatedDate
    private Instant createdDate;

    @Column
    @LastModifiedDate
    private Instant lastModifiedDate;

    private ShellKind shellKind;
    private String shellType;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shellId")
    Set<ShellExtension> shellExtensions= new HashSet<>();

    public void setShellExtensions(Set<ShellExtension> shellExtensions) {
        if(shellExtensions==null) {shellExtensions = new HashSet<>();}
        this.shellExtensions = shellExtensions;
        for(ShellExtension s : shellExtensions) {
            s.setShellId(this);
        }
    }

    public void setDisplayNames(Set<ShellDisplayName> displayNames) {
        if(displayNames==null) {displayNames = new HashSet<>();}
        this.displayNames = displayNames;
        for(ShellDisplayName s : displayNames) {
            s.setShellId(this);
        }
    }

    public void setSubmodels(Set<Submodel> submodels) {
        if(submodels==null) {submodels = new HashSet<>();}
        this.submodels = submodels;
        for(Submodel s : submodels) {
            s.setShellId(this);
        }
    }

    public void add(Submodel submodel) {
        if (submodel != null) {
            if (submodels == null) {
                submodels = new HashSet<>();
            }
            submodels.add(submodel);
            submodel.setShellId(this);
        }
    }

    public void setIdentifiers(Set<ShellIdentifier> identifiers) {
        if(identifiers==null) {identifiers = new HashSet<>();}
        this.identifiers = identifiers;
        for(ShellIdentifier s : identifiers) {
            s.setShellId(this);
        }
    }

    public void setDescriptions(Set<ShellDescription> descriptions) {
        if(descriptions==null) {descriptions = new HashSet<>();}
        this.descriptions = descriptions;
        for(ShellDescription s : descriptions) {
            s.setShellId(this);
        }
    }
}
