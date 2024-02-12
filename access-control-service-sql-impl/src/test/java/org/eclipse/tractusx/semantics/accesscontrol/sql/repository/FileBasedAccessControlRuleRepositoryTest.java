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

package org.eclipse.tractusx.semantics.accesscontrol.sql.repository;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataRetrievalFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;

class FileBasedAccessControlRuleRepositoryTest {

   private static final String PUBLIC_READABLE = "PUBLIC_READABLE";
   private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

   public static Stream<Arguments> bpnFilteringProvider() {
      return Stream.<Arguments> builder()
            .add( Arguments.of( "BPNL00000000000A", List.of( 1L, 3L ) ) )
            .add( Arguments.of( "BPNL00000000000B", List.of() ) )
            .add( Arguments.of( "BPNL00000000000C", List.of( 2L ) ) )
            .build();
   }

   @SuppressWarnings( "DataFlowIssue" )
   @ParameterizedTest
   @MethodSource( "bpnFilteringProvider" )
   void testFindAllByBpnWithinValidityPeriodExpectFilteredResults( final String bpn, final List<Long> expectedRuleIds ) {
      final var filePath = Path.of( getClass().getResource( "/example-access-rules.json" ).getFile() );
      final var underTest = new FileBasedAccessControlRuleRepository( objectMapper, filePath.toAbsolutePath().toString() );

      List<AccessRule> actual = underTest.findAllByBpnWithinValidityPeriod( bpn, PUBLIC_READABLE );

      final var actualIds = actual.stream().map( AccessRule::getId ).toList();
      assertThat( actualIds ).isEqualTo( expectedRuleIds );
   }

   @Test
   void testFindAllByBpnWithinValidityPeriodWithMissingResourceExpectException() {
      final var filePath = Path.of( "unknown.json" );
      final var underTest = new FileBasedAccessControlRuleRepository( objectMapper, filePath.toAbsolutePath().toString() );

      assertThatThrownBy( () -> underTest.findAllByBpnWithinValidityPeriod( "BPNL00000000000A", PUBLIC_READABLE ) )
            .isInstanceOf( DataRetrievalFailureException.class );
   }
}