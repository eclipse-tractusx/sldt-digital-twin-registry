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
package org.eclipse.tractusx.semantics.registry.controller;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.tractusx.semantics.aas.registry.api.DescriptionApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.api.LookupApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.api.ShellDescriptorsApiDelegate;
import org.eclipse.tractusx.semantics.aas.registry.model.*;
import org.eclipse.tractusx.semantics.registry.dto.ShellCollectionDto;
import org.eclipse.tractusx.semantics.registry.dto.SubmodelCollectionDto;
import org.eclipse.tractusx.semantics.registry.mapper.ShellMapper;
import org.eclipse.tractusx.semantics.registry.mapper.SubmodelMapper;
import org.eclipse.tractusx.semantics.registry.model.Shell;
import org.eclipse.tractusx.semantics.registry.model.ShellIdentifier;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.service.ShellService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.NativeWebRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AssetAdministrationShellApiDelegate implements DescriptionApiDelegate, ShellDescriptorsApiDelegate, LookupApiDelegate {

    private final ShellService shellService;
    private final ShellMapper shellMapper;
    private final SubmodelMapper submodelMapper;

    public AssetAdministrationShellApiDelegate(final ShellService shellService,
                                               final ShellMapper shellMapper,
                                               final SubmodelMapper submodelMapper) {
        this.shellService = shellService;
        this.shellMapper = shellMapper;
        this.submodelMapper = submodelMapper;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return DescriptionApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<ServiceDescription> getDescription() {
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setProfiles( List.of( ServiceDescription.ProfilesEnum.ASSETADMINISTRATIONSHELLREGISTRYSERVICESPECIFICATION_SSP_001, ServiceDescription.ProfilesEnum.DISCOVERYSERVICESPECIFICATION_SSP_001) );
        return  new ResponseEntity<>( serviceDescription, HttpStatus.OK );
    }

    @Override
    public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById( byte[]  aasIdentifier ) {
        shellService.deleteShell( getDecodedId(aasIdentifier) );
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @Override
    public ResponseEntity<Void> deleteAllAssetLinksById(byte[] aasIdentifier) {

        shellService.deleteAllIdentifiers(getDecodedId(aasIdentifier));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @Override
    public ResponseEntity<Void> deleteSubmodelDescriptorByIdThroughSuperpath( byte[] aasIdentifier, byte[] submodelIdentifier, @RequestHeader String externalSubjectId ) {
        shellService.deleteSubmodel(getDecodedId( aasIdentifier ), getDecodedId( submodelIdentifier ),getExternalSubjectIdOrEmpty( externalSubjectId ));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @Override
    public ResponseEntity<GetAssetAdministrationShellDescriptorsResult> getAllAssetAdministrationShellDescriptors( Integer limit, String cursor,
          AssetKind assetKind, String assetType, @RequestHeader String externalSubjectId ) {
        ShellCollectionDto dto =  shellService.findAllShells(limit, cursor,getExternalSubjectIdOrEmpty(externalSubjectId));
        GetAssetAdministrationShellDescriptorsResult result = shellMapper.toApiDto(dto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetSubmodelDescriptorsResult> getAllSubmodelDescriptorsThroughSuperpath( byte[] aasIdentifier, Integer limit, String cursor, @RequestHeader String externalSubjectId  ) {
        Shell savedShell = shellService.findShellByExternalIdAndExternalSubjectId(getDecodedId( aasIdentifier ),getExternalSubjectIdOrEmpty(externalSubjectId));
        SubmodelCollectionDto dto = shellService.findAllSubmodel( limit,cursor, savedShell);
        GetSubmodelDescriptorsResult result= submodelMapper.toApiDto( dto );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById( byte[] aasIdentifier, @RequestHeader String externalSubjectId ) {
        String decodedAasIdentifier = getDecodedId( aasIdentifier );
        Shell saved = shellService.findShellByExternalIdAndExternalSubjectId(decodedAasIdentifier, getExternalSubjectIdOrEmpty(externalSubjectId));
           return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorByIdThroughSuperpath( byte[] aasIdentifier, byte[]  submodelIdentifier, @RequestHeader String externalSubjectId ) {
        Submodel submodel = shellService.findSubmodelByExternalId(getDecodedId( aasIdentifier ), getDecodedId( submodelIdentifier ),getExternalSubjectIdOrEmpty( externalSubjectId ));
        return new ResponseEntity<>(submodelMapper.toApiDto(submodel), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor( AssetAdministrationShellDescriptor assetAdministrationShellDescriptor ) {
        Shell shell = shellMapper.fromApiDto(assetAdministrationShellDescriptor);
        shellService.mapShellCollection( shell );
        if(!shell.getSubmodels().isEmpty()) shellService.mapSubmodel( shell.getSubmodels() );
        Shell saved = shellService.save(shell);
        return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptorThroughSuperpath( byte[] aasIdentifier, @RequestHeader String externalSubjectId, SubmodelDescriptor submodelDescriptor ) {
        Submodel toBeSaved = submodelMapper.fromApiDto(submodelDescriptor);
        toBeSaved.setIdExternal( submodelDescriptor.getId() );
        shellService.mapSubmodel( Set.of(toBeSaved) );
        Submodel savedSubModel = shellService.save(getDecodedId( aasIdentifier ), toBeSaved, getExternalSubjectIdOrEmpty(externalSubjectId));
        return new ResponseEntity<>(submodelMapper.toApiDto(savedSubModel), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> putAssetAdministrationShellDescriptorById( byte[] aasIdentifier, AssetAdministrationShellDescriptor assetAdministrationShellDescriptor, @RequestHeader String externalSubjectId ) {
        Shell shell = shellMapper.fromApiDto( assetAdministrationShellDescriptor );
        Shell shellFromDb = shellService.findShellByExternalId( getDecodedId( aasIdentifier),getExternalSubjectIdOrEmpty(externalSubjectId) );
        shellService.update( shell.withId( shellFromDb.getId() ).withIdExternal(getDecodedId(aasIdentifier)  ),getDecodedId(aasIdentifier));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> putSubmodelDescriptorByIdThroughSuperpath( byte[] aasIdentifier, byte[] submodelIdentifier, @RequestHeader String externalSubjectId, SubmodelDescriptor submodelDescriptor ) {
        shellService.deleteSubmodel(getDecodedId( aasIdentifier ),  getDecodedId( submodelIdentifier ),getExternalSubjectIdOrEmpty( externalSubjectId ));
        submodelDescriptor.setId( getDecodedId( submodelIdentifier ));
        postSubmodelDescriptorThroughSuperpath(aasIdentifier,externalSubjectId,submodelDescriptor);
        return new ResponseEntity<>( HttpStatus.NO_CONTENT );
    }

    @Override
    public ResponseEntity<GetAllAssetAdministrationShellIdsByAssetLink200Response> getAllAssetAdministrationShellIdsByAssetLink(List<byte[]> assetIds,
    Integer limit, String cursor, @RequestHeader String externalSubjectId) {
        if (assetIds == null || assetIds.isEmpty()) {
            return new ResponseEntity<>(new GetAllAssetAdministrationShellIdsByAssetLink200Response(), HttpStatus.OK);
        }

        List<SpecificAssetId> listSpecificAssetId =assetIds.stream().map( this::decodeSAID).collect( Collectors.toList());
        GetAllAssetAdministrationShellIdsByAssetLink200Response result  =
              shellService.findExternalShellIdsByIdentifiersByExactMatch(shellMapper.fromApiDto(listSpecificAssetId), limit, cursor,getExternalSubjectIdOrEmpty(externalSubjectId));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private SpecificAssetId decodeSAID(byte[] encodedId){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL);
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode( encodedId );
            return mapper.readValue(decodedBytes, SpecificAssetId.class );
        } catch (Exception e ) {
            throw new IllegalArgumentException("Incorrect Base64 encoded value provided as parameter");
        }
    }

    @Override
    public ResponseEntity<List<SpecificAssetId>> getAllAssetLinksById(byte[] aasIdentifier,@RequestHeader String externalSubjectId) {
            Set<ShellIdentifier> identifiers = shellService.findShellIdentifiersByExternalShellId(getDecodedId( aasIdentifier ),getExternalSubjectIdOrEmpty(externalSubjectId));
            return new ResponseEntity<>(shellMapper.toApiDto(identifiers), HttpStatus.OK);
        }

    @Override
    public ResponseEntity<List<SpecificAssetId>> postAllAssetLinksById(byte[] aasIdentifier, List<SpecificAssetId> specificAssetId, @RequestHeader String externalSubjectId ) {
        Set<ShellIdentifier> shellIdentifiers = shellService.save(getDecodedId( aasIdentifier ), shellMapper.fromApiDto(specificAssetId),getExternalSubjectIdOrEmpty( externalSubjectId ));
        List<SpecificAssetId> list = shellMapper.toApiDto(shellIdentifiers);
        return new ResponseEntity<>(list, HttpStatus.CREATED);
    }

    /**
     * Since /query is not part of AAS 3.0, so this method is not used.
     * Keeping it for the reason that it might come up in next version.
     */
    @Deprecated
    public ResponseEntity<List<String>> postQueryAllAssetAdministrationShellIds(ShellLookup shellLookup,@RequestHeader String externalSubjectId) {
        List<SpecificAssetId> assetIds = shellLookup.getQuery().getAssetIds();
        List<String> externalIds = shellService.findExternalShellIdsByIdentifiersByAnyMatch(shellMapper.fromApiDto(assetIds),getExternalSubjectIdOrEmpty(externalSubjectId));
        return new ResponseEntity<>(externalIds, HttpStatus.OK);
    }

    private String getExternalSubjectIdOrEmpty(String externalSubjectId) {
            return (null ==externalSubjectId) ? "" : externalSubjectId;
        }

    private String getDecodedId( byte[] aasIdentifier ) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode( aasIdentifier );
            return new String( decodedBytes );
        }catch ( Exception e ){
            throw new IllegalArgumentException("Incorrect Base64 encoded value provided as parameter");
        }
    }
    }

