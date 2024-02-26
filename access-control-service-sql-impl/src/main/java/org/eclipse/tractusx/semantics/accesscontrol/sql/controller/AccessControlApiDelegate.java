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

package org.eclipse.tractusx.semantics.accesscontrol.sql.controller;

import java.util.List;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.converter.AccessRuleMapper;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.api.AccessControlsApiDelegate;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.CreateAccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.GetAllAccessRules200Response;
import org.eclipse.tractusx.semantics.accesscontrol.sql.rest.model.ReadUpdateAccessRule;
import org.eclipse.tractusx.semantics.accesscontrol.sql.service.AccessControlPersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AccessControlApiDelegate implements AccessControlsApiDelegate {

   private final AccessControlPersistenceService accessControlPersistenceService;
   private final AccessRuleMapper accessRuleMapper;

   public AccessControlApiDelegate( final AccessControlPersistenceService accessControlPersistenceService, AccessRuleMapper accessRuleMapper ) {
      this.accessControlPersistenceService = accessControlPersistenceService;
      this.accessRuleMapper = accessRuleMapper;
   }

   @Override
   public ResponseEntity<ReadUpdateAccessRule> createNewAccessRule( CreateAccessRule createAccessRule ) {
      AccessRule savedRule = accessControlPersistenceService.saveRule( accessRuleMapper.map( createAccessRule ) );
      return ResponseEntity.status( HttpStatus.CREATED ).body( accessRuleMapper.map( savedRule ) );
   }

   @Override
   public ResponseEntity<Void> deleteAccessRuleByRuleId( Long ruleId ) {
      accessControlPersistenceService.deleteRule( ruleId );
      return ResponseEntity.status( HttpStatus.NO_CONTENT ).build();
   }

   @Override
   public ResponseEntity<ReadUpdateAccessRule> getAccessRuleByRuleId( Long ruleId ) {
      return accessControlPersistenceService.getRuleById( ruleId )
            .map( accessRuleMapper::map )
            .map( rule -> ResponseEntity.ok().body( rule ) )
            .orElse( ResponseEntity.notFound().build() );
   }

   @Override
   public ResponseEntity<GetAllAccessRules200Response> getAllAccessRules() {
      List<ReadUpdateAccessRule> items = accessControlPersistenceService.getAllRules().stream()
            .map( accessRuleMapper::map )
            .toList();
      GetAllAccessRules200Response response = new GetAllAccessRules200Response();
      response.setItems( items );
      return ResponseEntity.ok().body( response );
   }

   @Override
   public ResponseEntity<ReadUpdateAccessRule> updateAccessRuleByRuleId( Long ruleId, ReadUpdateAccessRule readUpdateAccessRule ) {
      AccessRule updatedRule = accessControlPersistenceService.updateRule( ruleId, accessRuleMapper.map( readUpdateAccessRule ) );
      return ResponseEntity.ok().body( accessRuleMapper.map( updatedRule ) );
   }
}
