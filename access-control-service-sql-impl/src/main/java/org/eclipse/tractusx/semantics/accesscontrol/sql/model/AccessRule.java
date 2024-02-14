/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.semantics.accesscontrol.sql.model;

import java.time.Instant;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.converter.AccessRulePolicyConverter;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnCreate;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnUpdate;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.ValidValidityPeriod;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Entity
@Table( name = "ACCESS_RULE" )
@Data
@ValidValidityPeriod( groups = { OnCreate.class, OnUpdate.class } )
public class AccessRule {

   public enum PolicyType {
      AAS
   }

   @Null( groups = OnCreate.class )
   @NotNull( groups = OnUpdate.class )
   @Id
   @GeneratedValue( strategy = GenerationType.AUTO )
   @Column( name = "ID", nullable = false, updatable = false )
   private Long id;

   @NotNull( groups = { OnCreate.class, OnUpdate.class } )
   @Column( name = "TID", nullable = false, updatable = false, length = 36 )
   private String tid;

   @NotNull( groups = { OnCreate.class, OnUpdate.class } )
   @NotBlank( groups = { OnCreate.class, OnUpdate.class } )
   @Column( name = "TARGET_TENANT", nullable = false, updatable = false, length = 36 )
   private String targetTenant;

   @NotNull( groups = { OnCreate.class, OnUpdate.class } )
   @Enumerated( EnumType.STRING )
   @Column( name = "POLICY_TYPE", nullable = false, length = 10 )
   private PolicyType policyType;

   @Valid
   @NotNull( groups = { OnCreate.class, OnUpdate.class } )
   @Lob
   @Column( name = "POLICY", nullable = false )
   @Convert( converter = AccessRulePolicyConverter.class )
   private AccessRulePolicy policy;

   @Column( name = "DESCRIPTION", length = 256 )
   private String description;

   @Column( name = "VALID_FROM" )
   private Instant validFrom;

   @Column( name = "VALID_TO" )
   private Instant validTo;

}
