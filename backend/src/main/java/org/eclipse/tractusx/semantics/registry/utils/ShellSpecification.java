/*******************************************************************************
 * Copyright (c) 2025 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.semantics.registry.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReference;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReferenceKey;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShellSpecification<T> implements Specification<T> {

   private final String sortFieldName;
   private final ShellCursor shellCursor;
   private final String tenantId;
   private final String owningTenantId;
   private final String publicWildcardPrefix;
   private final List<String> publicWildcardAllowedTypes;
   private final OffsetDateTime createdAfter;

   @Override
   public Predicate toPredicate( Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder criteriaBuilder ) {
      return applyFilter( root, cq, criteriaBuilder );
   }

   private Predicate applyFilter( Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder criteriaBuilder ) {
      if ( root.toString().contains( "Shell" ) ) {
         final Instant searchValue = getCreatedDate();

         if ( owningTenantId.equals( tenantId ) ) {
            return criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue );
         }

         return getAllShellsPredicate( root, cq, criteriaBuilder, searchValue );
      } else {
         UUID searchValue = shellCursor.getSubmodelSearchCursor();
         cq.orderBy( criteriaBuilder.asc( criteriaBuilder.coalesce( root.get( sortFieldName ),
               UUID.fromString( "00000000-0000-0000-0000-000000000000" ) ) ) );
         return criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue );
      }
   }

   /**
       * Retrieves the created date for filtering purposes.
       *
       * @return the created date as an Instant. If the cursor has not been received,
       *         it returns the createdAfter date if it is present,
       *         otherwise it returns the shell search cursor date.
       */
      private Instant getCreatedDate() {
         return shellCursor.hasCursorReceived() ?
               shellCursor.getShellSearchCursor() :
               Optional.ofNullable( createdAfter ).map( OffsetDateTime::toInstant ).orElseGet( shellCursor::getShellSearchCursor );

      }

   private Predicate getAllShellsPredicate( Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder criteriaBuilder, Instant searchValue ) {
      // Join Shell -> ShellIdentifier
      String t = Shell.Fields.identifiers;
      Join<Shell,ShellIdentifier > shellIdentifierShellJoin = root.join( Shell.Fields.identifiers );
      // join ShellIdentifier -> ShellIdentifierExternalSubjectReference -> ShellIdentifierExternalSubjectReferenceKey
      Join<ShellIdentifierExternalSubjectReference,ShellIdentifierExternalSubjectReferenceKey> referenceKeyJoin = shellIdentifierShellJoin.join( ShellIdentifier.Fields.externalSubjectId ).join( ShellIdentifierExternalSubjectReference.Fields.keys );

      return criteriaBuilder.and(
            criteriaBuilder.or(
                  criteriaBuilder.equal( referenceKeyJoin.get( ShellIdentifierExternalSubjectReferenceKey.Fields.value ), tenantId ),
                  criteriaBuilder.and(
                        criteriaBuilder.equal( referenceKeyJoin.get( ShellIdentifierExternalSubjectReferenceKey.Fields.value ), publicWildcardPrefix ),
                        criteriaBuilder.in( shellIdentifierShellJoin.get( ShellIdentifier.Fields.key ) ).value( publicWildcardAllowedTypes )
                  )
            ),
            criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue )
      );
   }
}
