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
package org.eclipse.tractusx.semantics.registry.utils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReference;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifierExternalSubjectReferenceKey;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShellSpecification<T> implements Specification<T> {

   private final String sortFieldName;
   private final ShellCursor shellCursor;
   private final String tenantId;
   private final String owningTenantId;
   private final String publicWildcardPrefix;
   private final List<String> publicWildcardAllowedTypes;

   @Override
   public Predicate toPredicate( Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder criteriaBuilder ) {
      return applyFilter( root,cq, criteriaBuilder );
   }

   private Predicate applyFilter( Root<T> root,CriteriaQuery<?> cq, CriteriaBuilder criteriaBuilder ) {
      if(root.toString().contains( "Shell" )){
         Instant searchValue = shellCursor.getShellSearchCursor();
         cq.orderBy( criteriaBuilder.asc( criteriaBuilder.coalesce( root.get( sortFieldName ), Instant.now() ) ) );

         if(owningTenantId.equals( tenantId )){
            return criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue );
         }

         return getAllShellsPredicate(root, cq, criteriaBuilder, searchValue);
      }else{
         UUID searchValue = shellCursor.getSubmodelSearchCursor();
         cq.orderBy( criteriaBuilder.asc( criteriaBuilder.coalesce( root.get( sortFieldName ),
               UUID.fromString( "00000000-0000-0000-0000-000000000000" )) ) );
         return criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue );
      }
   }

   private Predicate getAllShellsPredicate(Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder criteriaBuilder, Instant searchValue ){
      // Query for noIdentifierSubQuery
      Subquery<String> noIdentifierSubQuery = cq.subquery(String.class);
      Root<ShellIdentifier> shellIdentifierRoot = noIdentifierSubQuery.from(ShellIdentifier.class);
      noIdentifierSubQuery
            .select(shellIdentifierRoot.get("shellId"))
            .where(criteriaBuilder.equal(shellIdentifierRoot.get("shellId"), root));

      // Query for identifierSubQuery
      Subquery<UUID> identifierSubQuery = cq.subquery(UUID.class);
      Subquery<UUID> identifierInnerSubQuery = cq.subquery(UUID.class);
      Root<ShellIdentifier> shellIdentifierRoot1 = identifierSubQuery.from(ShellIdentifier.class);
      Root<ShellIdentifierExternalSubjectReferenceKey> externalSubjectReferenceKeyRoot = identifierInnerSubQuery.from(ShellIdentifierExternalSubjectReferenceKey.class);
      identifierInnerSubQuery
            .select(externalSubjectReferenceKeyRoot.get("shellIdentifierExternalSubjectReference"))
            .where( criteriaBuilder.and(
                        criteriaBuilder.or(
                              criteriaBuilder.equal(externalSubjectReferenceKeyRoot.get("value"),tenantId),
                              criteriaBuilder.and(
                                    criteriaBuilder.equal(externalSubjectReferenceKeyRoot.get("value"),publicWildcardPrefix),
                                    criteriaBuilder.in(shellIdentifierRoot1.get("key")).value(publicWildcardAllowedTypes)
                              )
                        ),
                        criteriaBuilder.equal(externalSubjectReferenceKeyRoot.get("shellIdentifierExternalSubjectReference").get( "shellIdentifier" ),shellIdentifierRoot1)
                  )
            );

      identifierSubQuery
            .select(shellIdentifierRoot1.get("shellId").get("id"))
            .where(criteriaBuilder.exists(identifierInnerSubQuery));

      return criteriaBuilder.and(
            criteriaBuilder.or(
                  criteriaBuilder.not(criteriaBuilder.exists(noIdentifierSubQuery)),
                  criteriaBuilder.in(root.get("id")).value(identifierSubQuery)
            ),
            criteriaBuilder.greaterThan(root.get(sortFieldName), searchValue));
   }
}
