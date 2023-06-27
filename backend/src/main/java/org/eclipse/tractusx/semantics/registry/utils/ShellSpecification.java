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

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShellSpecification<T> implements Specification<T> {

   private final String sortFieldName;
   private final ShellCursor shellCursor;

   @Override
   public Predicate toPredicate( Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder criteriaBuilder ) {
      Predicate predicate = applyFilter( root, criteriaBuilder );
      cq.orderBy( criteriaBuilder.asc( root.get( sortFieldName ) ) );
      return predicate;
   }

   private Predicate applyFilter( Root<T> root, CriteriaBuilder criteriaBuilder ) {
      var searchValue = shellCursor.getSearchCursor();
      return criteriaBuilder.greaterThan( root.get( sortFieldName ), searchValue );
   }
}
