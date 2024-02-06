/*******************************************************************************
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH and others
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
 *
 ******************************************************************************/

package org.eclipse.tractusx.semantics.accesscontrol.sql.model;

import java.time.Instant;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.converter.AccessRulePolicyConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

//@Entity
//@Table(name = "access_control_rule")
@Data
public class AccessRule {

   public enum PolicyType {
      AAS
   }

   @Id
   @GeneratedValue( strategy = GenerationType.AUTO )
   @Column( name = "id", nullable = false, updatable = false )
   private Long id;

   @Column( name = "tid", nullable = false, updatable = false, length = 36 )
   private String tid;

   @Column( name = "target_tenant", nullable = false, updatable = false, length = 36 )
   private String targetTenant;

   @Enumerated( EnumType.STRING )
   @Column( name = "policy_type", nullable = false, length = 10 )
   private PolicyType policyType;

   @Lob
   @Column( name = "policy", nullable = false )
   @Convert( converter = AccessRulePolicyConverter.class )
   private AccessRulePolicy policy;

   @Column( name = "description", length = 256 )
   private String description;

   @Column( name = "valid_from" )
   private Instant validFrom;

   @Column( name = "valid_to" )
   private Instant validTo;

}
