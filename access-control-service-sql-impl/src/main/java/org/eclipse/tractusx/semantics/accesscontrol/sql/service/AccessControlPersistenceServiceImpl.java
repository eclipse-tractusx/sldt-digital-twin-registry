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

package org.eclipse.tractusx.semantics.accesscontrol.sql.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.repository.AccessControlRuleRepository;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnCreate;
import org.eclipse.tractusx.semantics.accesscontrol.sql.validation.OnUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@Service
public class AccessControlPersistenceServiceImpl implements AccessControlPersistenceService {

   private final AccessControlRuleRepository accessControlRuleRepository;
   private final Validator validator;
   private final String ownerTenant;

   public AccessControlPersistenceServiceImpl(
         AccessControlRuleRepository accessControlRuleRepository, Validator validator,
         @Value( "${registry.idm.owning-tenant-id:}" ) String ownerTenant ) {
      this.accessControlRuleRepository = accessControlRuleRepository;
      this.validator = validator;
      this.ownerTenant = Objects.requireNonNull( StringUtils.stripToNull( ownerTenant ), "OwnerTenantId is not set!" );
   }

   @Override
   @Transactional( propagation = Propagation.REQUIRED, readOnly = true )
   public List<AccessRule> getAllRules() {
      return accessControlRuleRepository.findAll();
   }

   @Override
   @Transactional( propagation = Propagation.REQUIRED, readOnly = true )
   public Optional<AccessRule> getRuleById( Long ruleId ) {
      return accessControlRuleRepository.findById( ruleId );
   }

   @Override
   @Transactional( propagation = Propagation.REQUIRED )
   public AccessRule saveRule( AccessRule rule ) {
      verifyOwnerTenantId( rule );
      Set<ConstraintViolation<AccessRule>> violations = validator.validate( rule, OnCreate.class );
      if ( !violations.isEmpty() ) {
         throw new ConstraintViolationException( violations );
      }
      return accessControlRuleRepository.saveAndFlush( rule );
   }

   @Override
   @Transactional( propagation = Propagation.REQUIRED )
   public AccessRule updateRule( Long ruleId, AccessRule rule ) {
      verifyOwnerTenantId( rule );
      verifyRuleId( ruleId, rule );
      Set<ConstraintViolation<AccessRule>> violations = validator.validate( rule, OnUpdate.class );
      if ( !violations.isEmpty() ) {
         throw new ConstraintViolationException( violations );
      }
      final AccessRule entity = accessControlRuleRepository.findById( ruleId )
            .orElseThrow( () -> new IllegalStateException( "Rule with Id: " + ruleId + " cannot be updated as it does not exist!" ) );
      entity.setTid( rule.getTid() );
      entity.setTargetTenant( rule.getTargetTenant() );
      entity.setPolicy( rule.getPolicy() );
      entity.setPolicyType( rule.getPolicyType() );
      entity.setDescription( rule.getDescription() );
      entity.setValidFrom( rule.getValidFrom() );
      entity.setValidTo( rule.getValidTo() );
      return accessControlRuleRepository.saveAndFlush( entity );
   }

   @Override
   @Transactional( propagation = Propagation.REQUIRED )
   public void deleteRule( Long ruleId ) {
      accessControlRuleRepository.deleteById( ruleId );
   }

   private void verifyRuleId( Long ruleId, AccessRule rule ) {
      if ( !Objects.equals( rule.getId(), ruleId ) ) {
         throw new IllegalArgumentException( "RuleId must match the rule.id value!" );
      }
   }

   private void verifyOwnerTenantId( AccessRule rule ) {
      if ( !Objects.equals( rule.getTid(), ownerTenant ) ) {
         throw new IllegalArgumentException( "TenantId must match the Id of the owner tenant: " + ownerTenant );
      }
   }
}
