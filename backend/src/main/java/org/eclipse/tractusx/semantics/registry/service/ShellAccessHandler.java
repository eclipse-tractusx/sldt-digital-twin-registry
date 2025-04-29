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

package org.eclipse.tractusx.semantics.registry.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.eclipse.tractusx.semantics.accesscontrol.api.exception.DenyAccessException;
import org.eclipse.tractusx.semantics.accesscontrol.api.model.SpecificAssetId;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.projection.ShellIdentifierMinimal;
import org.eclipse.tractusx.semantics.registry.utils.ShellCursor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

public interface ShellAccessHandler {

   @Nullable
   Shell filterShellProperties( Shell shell, String externalSubjectId );

   List<Shell> filterListOfShellProperties( List<Shell> shells, String externalSubjectId );

   Specification<Shell> shellFilterSpecification( String sortFieldName, ShellCursor cursor, String externalSubjectId, OffsetDateTime createdAfter );

   default List<String> filterToVisibleShellIdsForLookup( final Set<SpecificAssetId> userQuery, final List<ShellIdentifierMinimal> shellIdentifiers,
         final String externalSubjectId )
         throws DenyAccessException {
      throw new UnsupportedOperationException( "Only supported in case of granular access control." );
   }

   default boolean supportsGranularAccessControl() {
      return false;
   }
}
