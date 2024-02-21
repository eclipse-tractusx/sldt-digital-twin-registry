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

package org.eclipse.tractusx.semantics.registry.utils;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShellCursor {
   private int recordSize;
   private final String nextPageCursor;

   public boolean hasCursorReceived() {
      return nextPageCursor != null && !nextPageCursor.isEmpty();
   }

   public Instant getDecodedInstant( String encodedCursorValue ) {
      String value = getDecodedValue( encodedCursorValue );
      try {
         return Instant.parse( value );
      }catch ( Exception e ){throw new IllegalArgumentException("Invalid cursor value");}
   }

   private String getDecodedValue( String encodedCursorValue ) {
      if ( encodedCursorValue == null || encodedCursorValue.isEmpty() ) {
         throw new IllegalArgumentException( "Given Cursor is not valid." );
      }
      var decodedBytes = Base64.getDecoder().decode( encodedCursorValue );
      var decodedValue = new String( decodedBytes );
      return substringBetween( decodedValue, "*" );
   }

   public String getEncodedCursorShell( Instant field, boolean hasNextElements ) {
      requireNonNull( field );
      if ( !hasNextElements )
         return null;
      var valueToEncode = "*" + field + "* - " + LocalDateTime.now();
      return Base64.getEncoder().encodeToString( valueToEncode.getBytes() );
   }

   public String getEncodedCursorSubmodel( UUID field, boolean hasNextElements ) {
      requireNonNull( field );
      if ( !hasNextElements )
         return null;
      var valueToEncode = "*" + field + "* - " + LocalDateTime.now();
      return Base64.getEncoder().encodeToString( valueToEncode.getBytes() );
   }

   public Instant getShellSearchCursor() {
      if ( !hasCursorReceived() )
         return ZonedDateTime.now().minusYears( 5 ).toInstant();

      return getDecodedInstant( nextPageCursor );
   }

   public UUID getSubmodelSearchCursor() {
      if ( !hasCursorReceived() )
         return UUID.fromString("00000000-0000-0000-0000-000000000000");
      return UUID.fromString( getDecodedValue( nextPageCursor ));
   }
}
