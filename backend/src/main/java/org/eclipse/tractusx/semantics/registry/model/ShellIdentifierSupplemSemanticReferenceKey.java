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

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table
@NoArgsConstructor
@AllArgsConstructor
@With
public class ShellIdentifierSupplemSemanticReferenceKey {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   UUID id;
   ReferenceKeyType type;
   @Column(name = "ref_key_value")
   String value;

   @JsonBackReference
   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "fk_reference_id")
   private ShellIdentifierSupplemSemanticReference shellIdentifierSupplemSemanticReference;

   @JsonBackReference
   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "fk_reference_parent_id")
   private ShellIdentifierSupplemSemanticReferenceParent shellIdentifierSupplemSemanticReferenceParent;
}


