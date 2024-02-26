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

package org.eclipse.tractusx.semantics.accesscontrol.sql.model.converter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessRuleMapper {

   private final String ownerTenant;

   public CustomAccessRuleMapper( @Value( "${registry.idm.owning-tenant-id:}" ) String ownerTenant ) {
      this.ownerTenant = StringUtils.stripToNull( ownerTenant );
   }

   @AfterMapping
   public void calledWithTarget( @MappingTarget AccessRule target ) {
      target.setTargetTenant( target.getPolicy().getBpn() );
      // only fill Tid if null to avoid overwrites in case of updates
      if ( target.getTid() == null ) {
         target.setTid( ownerTenant );
      }
   }
}
