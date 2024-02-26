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

package org.eclipse.tractusx.semantics.accesscontrol.sql.validation;

import java.time.Instant;
import java.util.Optional;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidityPeriodValidator implements ConstraintValidator<ValidValidityPeriod, AccessRule> {
   private String message;

   @Override
   public void initialize( ValidValidityPeriod constraintAnnotation ) {
      ConstraintValidator.super.initialize( constraintAnnotation );
      this.message = constraintAnnotation.message();
   }

   @Override
   public boolean isValid( AccessRule rule, ConstraintValidatorContext constraintValidatorContext ) {
      var valid = true;
      final var from = Optional.ofNullable( rule.getValidFrom() )
            .map( Instant::toEpochMilli )
            .orElse( 0L );
      final var to = Optional.ofNullable( rule.getValidTo() )
            .map( Instant::toEpochMilli )
            .orElse( Long.MAX_VALUE );
      if ( from >= to ) {
         constraintValidatorContext.buildConstraintViolationWithTemplate( "ValidFrom must be earlier than validTo!" )
               .addPropertyNode( "validFrom" ).addConstraintViolation();
         constraintValidatorContext.buildConstraintViolationWithTemplate( "ValidTo must be later than validFrom!" )
               .addPropertyNode( "validTo" ).addConstraintViolation();
         valid = false;
      }
      if ( !valid ) {
         constraintValidatorContext.buildConstraintViolationWithTemplate( message ).addConstraintViolation();
      }
      return valid;
   }
}
