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

package org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.DtrDataPlaneAccessControlService.*;
import static org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.DtrAccessVerificationClient.HEADER_EDC_BPN;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.edc.spi.iam.ClaimToken;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.tractusx.semantics.edc.dataplane.http.accesscontrol.client.DtrAccessVerificationClient;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance( TestInstance.Lifecycle.PER_METHOD )
class DtrDataPlaneAccessControlServiceTest {

   private static final String BPN_0001 = "BPN0001";

   final Monitor monitor = mock();
   final ClaimToken claimToken = mock();
   final UriRoutingContext uriInfo = mock( UriRoutingContext.class );
   final DataAddress address = mock();
   final HttpAccessControlCheckClientConfig config = mock();
   final HttpAccessControlCheckDtrClientConfig dtrConfig = mock();
   final DtrAccessVerificationClient client = mock();
   DtrDataPlaneAccessControlService underTest;

   public static Stream<Arguments> aspectModelParameterProvider() {
      return Stream.<Arguments> builder()
            .add( Arguments.of(
                  "http://backend.example.com",
                  "api/submodel-resource",
                  "param1=value1",
                  "http://backend.example.com/api/submodel-resource",
                  "http://edc.example.com/public/v2/api/submodel-resource?param1=value1"
            ) )
            .add( Arguments.of(
                  "http://backend.example.com/",
                  "api/submodel-resource",
                  "param1=value1",
                  "http://backend.example.com/api/submodel-resource",
                  "http://edc.example.com/public/v2/api/submodel-resource?param1=value1"
            ) )
            .build();
   }

   @BeforeEach
   void initMocks() {
      when( config.getEdcDataPlaneBaseUrl() ).thenReturn( "http://edc.example.com/" );
      when( dtrConfig.getAspectModelUrlPattern() ).thenReturn( "http:\\/\\/backend\\.example\\.com\\/api\\/.*" );
      when( config.getDtrClientConfigMap() ).thenReturn( Map.of( "0", dtrConfig ) );

      underTest = new DtrDataPlaneAccessControlService( monitor, Map.of( "0", client ), config );
   }

   @Test
   void test_DtrLookupCall_ShouldSucceed_WhenBpnHeaderIsPresent() {
      //given
      final Map<String, Object> additionalData = Map.of();
      final Map<String, Object> requestData = Map.of( "method", "GET", "path", uriInfo );
      when( address.getStringProperty( ADDRESS_ASSET_BASE_URL ) ).thenReturn( "http://dtr.example.com" );
      when( address.getStringProperty( ADDRESS_HEADER_EDC_BPN ) ).thenReturn( BPN_0001 );
      when( client.isAspectModelCall( anyString() ) ).thenReturn( false );
      when( uriInfo.getRequestUri() ).thenReturn( URI.create( "http://edc.example.com/public/v2/api/dtr/resource?param1=value1" ) );

      //when
      final Result<Void> actual = underTest.checkAccess( claimToken, address, requestData, additionalData );

      //then
      verify( address ).getStringProperty( ADDRESS_HEADER_EDC_BPN );
      verify( address ).getStringProperty( ADDRESS_ASSET_BASE_URL );
      assertThat( actual ).isNotNull();
      assertThat( actual.succeeded() ).isTrue();
   }

   @Test
   void test_DtrLookupCall_ShouldSucceed_WhenBpnHeaderIsMissing() {
      final Map<String, Object> additionalData = Map.of();
      final Map<String, Object> requestData = Map.of( "method", "GET", "path", uriInfo );
      when( address.getStringProperty( ADDRESS_ASSET_BASE_URL ) ).thenReturn( "http://dtr.example.com" );
      when( address.getStringProperty( ADDRESS_HEADER_EDC_BPN ) ).thenReturn( null );
      when( client.isAspectModelCall( anyString() ) ).thenReturn( false );
      when( uriInfo.getRequestUri() ).thenReturn( URI.create( "http://edc.example.com/public/v2/api/dtr/resource?param1=value1" ) );

      //when
      final Result<Void> actual = underTest.checkAccess( claimToken, address, requestData, additionalData );

      //then
      verify( address ).getStringProperty( ADDRESS_HEADER_EDC_BPN );
      verify( address ).getStringProperty( ADDRESS_ASSET_BASE_URL );
      assertThat( actual ).isNotNull();
      assertThat( actual.succeeded() ).isTrue();
   }

   @ParameterizedTest
   @MethodSource( "aspectModelParameterProvider" )
   void test_AspectModelBackendRequest_ShouldSucceed_WhenBpnHeaderIsPresentAndDtrRespondsWithOk(
         String assetBaseUrl, String proxyPath, String queryString, String fullBackendUrl, String fullDataPlaneUrl ) {
      //given
      final Map<String, Object> additionalData = Map.of();
      final Map<String, Object> requestData = Map.of( "method", "GET", "path", uriInfo );
      when( address.getStringProperty( ADDRESS_ASSET_BASE_URL ) ).thenReturn( assetBaseUrl );
      when( address.getStringProperty( ADDRESS_HEADER_EDC_BPN ) ).thenReturn( BPN_0001 );
      when( client.isAspectModelCall( fullBackendUrl ) ).thenReturn( true );
      when( uriInfo.getRequestUri() ).thenReturn( URI.create( fullDataPlaneUrl ) );
      when( client.shouldAllowAccess( proxyPath, queryString, Map.of( HEADER_EDC_BPN, BPN_0001 ) ) ).thenReturn( true );

      //when
      final Result<Void> actual = underTest.checkAccess( claimToken, address, requestData, additionalData );

      //then
      verify( monitor, never() ).info( anyString() );
      verify( client ).isAspectModelCall( fullBackendUrl );
      verify( client ).shouldAllowAccess( proxyPath, queryString, Map.of( HEADER_EDC_BPN, BPN_0001 ) );
      verify( address ).getStringProperty( ADDRESS_HEADER_EDC_BPN );
      verify( address ).getStringProperty( ADDRESS_ASSET_BASE_URL );
      assertThat( actual ).isNotNull();
      assertThat( actual.succeeded() ).isTrue();
   }

   @ParameterizedTest
   @MethodSource( "aspectModelParameterProvider" )
   void test_AspectModelBackendRequest_ShouldReturnFailure_WhenBpnHeaderIsPresentAndDtrRespondsWithError(
         String assetBaseUrl, String proxyPath, String queryString, String fullBackendUrl, String fullDataPlaneUrl ) {
      //given
      final Map<String, Object> additionalData = Map.of();
      final Map<String, Object> requestData = Map.of( "method", "GET", "path", uriInfo );
      when( address.getStringProperty( ADDRESS_ASSET_BASE_URL ) ).thenReturn( assetBaseUrl );
      when( address.getStringProperty( ADDRESS_HEADER_EDC_BPN ) ).thenReturn( BPN_0001 );
      when( client.isAspectModelCall( fullBackendUrl ) ).thenReturn( true );
      when( uriInfo.getRequestUri() ).thenReturn( URI.create( fullDataPlaneUrl ) );
      when( client.shouldAllowAccess( proxyPath, queryString, Map.of( HEADER_EDC_BPN, BPN_0001 ) ) ).thenReturn( false );

      //when
      final Result<Void> actual = underTest.checkAccess( claimToken, address, requestData, additionalData );

      //then
      verify( monitor, never() ).info( anyString() );
      verify( client ).isAspectModelCall( fullBackendUrl );
      verify( client ).shouldAllowAccess( proxyPath, queryString, Map.of( HEADER_EDC_BPN, BPN_0001 ) );
      verify( address ).getStringProperty( ADDRESS_HEADER_EDC_BPN );
      verify( address ).getStringProperty( ADDRESS_ASSET_BASE_URL );
      assertThat( actual ).isNotNull();
      assertThat( actual.failed() ).isTrue();
   }

   @ParameterizedTest
   @MethodSource( "aspectModelParameterProvider" )
   void test_AspectModelBackendRequest_ShouldReturnFailure_WhenBpnHeaderIsMissing(
         String assetBaseUrl, String proxyPath, String queryString, String fullBackendUrl, String fullDataPlaneUrl ) {
      //given
      final Map<String, Object> additionalData = Map.of();
      final Map<String, Object> requestData = Map.of( "method", "GET", "path", uriInfo );
      when( address.getStringProperty( ADDRESS_ASSET_BASE_URL ) ).thenReturn( assetBaseUrl );
      when( address.getStringProperty( ADDRESS_HEADER_EDC_BPN ) ).thenReturn( null );
      when( client.isAspectModelCall( fullBackendUrl ) ).thenReturn( true );
      when( uriInfo.getRequestUri() ).thenReturn( URI.create( fullDataPlaneUrl ) );
      when( client.shouldAllowAccess( proxyPath, queryString, Collections.emptyMap() ) ).thenReturn( false );

      //when
      final Result<Void> actual = underTest.checkAccess( claimToken, address, requestData, additionalData );

      //then
      verify( monitor, never() ).info( anyString() );
      verify( client ).isAspectModelCall( fullBackendUrl );
      verify( client ).shouldAllowAccess( proxyPath, queryString, Collections.emptyMap() );
      verifyNoMoreInteractions( client );
      verify( address ).getStringProperty( ADDRESS_HEADER_EDC_BPN );
      verify( address ).getStringProperty( ADDRESS_ASSET_BASE_URL );
      assertThat( actual ).isNotNull();
      assertThat( actual.failed() ).isTrue();
   }

   @ParameterizedTest
   @MethodSource( "aspectModelParameterProvider" )
   void test_AspectModelBackendRequest_ShouldReturnFailure_WhenBpnHeaderIsPresentButDtrDeniesAccess(
         String assetBaseUrl, String proxyPath, String queryString, String fullBackendUrl, String fullDataPlaneUrl ) {
      //given
      final Map<String, Object> additionalData = Map.of();
      final Map<String, Object> requestData = Map.of( "method", "GET", "path", uriInfo );
      when( address.getStringProperty( ADDRESS_ASSET_BASE_URL ) ).thenReturn( assetBaseUrl );
      when( address.getStringProperty( ADDRESS_HEADER_EDC_BPN ) ).thenReturn( BPN_0001 );
      when( client.isAspectModelCall( fullBackendUrl ) ).thenReturn( true );
      when( uriInfo.getRequestUri() ).thenReturn( URI.create( fullDataPlaneUrl ) );
      when( client.shouldAllowAccess( proxyPath, queryString, Map.of( HEADER_EDC_BPN, BPN_0001 ) ) ).thenReturn( false );

      //when
      final Result<Void> actual = underTest.checkAccess( claimToken, address, requestData, additionalData );

      //then
      verify( monitor, never() ).info( anyString() );
      verify( client ).isAspectModelCall( fullBackendUrl );
      verify( client ).shouldAllowAccess( proxyPath, queryString, Map.of( HEADER_EDC_BPN, BPN_0001 ) );
      verifyNoMoreInteractions( client );
      verify( address ).getStringProperty( ADDRESS_HEADER_EDC_BPN );
      verify( address ).getStringProperty( ADDRESS_ASSET_BASE_URL );
      assertThat( actual ).isNotNull();
      assertThat( actual.failed() ).isTrue();
   }

}
