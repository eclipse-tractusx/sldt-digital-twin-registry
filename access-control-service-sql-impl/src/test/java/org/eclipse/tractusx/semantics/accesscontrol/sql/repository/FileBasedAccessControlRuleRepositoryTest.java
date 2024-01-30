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

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.eclipse.tractusx.semantics.accesscontrol.sql.model.AccessRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataRetrievalFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;

class FileBasedAccessControlRuleRepositoryTest {

   private final ObjectMapper objectMapper = new ObjectMapper();

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
   void testFindAllByBpnExpectFilteredResults( final String bpn, final List<String> expectedRuleIds ) {
      final var filePath = Path.of( getClass().getResource( "/example-access-rules.json" ).getFile() );
      final var underTest = new FileBasedAccessControlRuleRepository( objectMapper, filePath.toAbsolutePath().toString() );

      List<AccessRule> actual = underTest.findAllByBpn( bpn );

      final var actualIds = actual.stream().map( AccessRule::getId ).toList();
      Assertions.assertThat( actualIds ).isEqualTo( expectedRuleIds );
   }

   @Test
   void testFindAllByBpnWithMissingResourceExpectException() {
      final var filePath = Path.of( "unknown.json" );
      final var underTest = new FileBasedAccessControlRuleRepository( objectMapper, filePath.toAbsolutePath().toString() );

      Assertions.assertThatThrownBy( () -> underTest.findAllByBpn( "BPNL00000000000A" ) )
            .isInstanceOf( DataRetrievalFailureException.class );
   }
}