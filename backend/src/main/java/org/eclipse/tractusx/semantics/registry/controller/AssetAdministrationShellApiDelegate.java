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

import java.util.*;

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
    // TODO: 21.06.2023 implement correct and not just give back dummy, implement test cases for this endpoint 
    public ResponseEntity<ServiceDescription> getDescription() {
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setProfiles( List.of( ServiceDescription.ProfilesEnum.ASSETADMINISTRATIONSHELLREPOSITORYSERVICESPECIFICATION_V3_0_MINIMALPROFILE, 
              ServiceDescription.ProfilesEnum.REGISTRYSERVICESPECIFICATION_V3_0) );
        return  new ResponseEntity<>( serviceDescription, HttpStatus.OK );
    }

    @Override
    public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById( String  aasIdentifier ) {
        shellService.deleteShell( aasIdentifier );
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @Override
    public ResponseEntity<Void> deleteAllAssetLinksById(String aasIdentifier) {
        shellService.deleteAllIdentifiers(aasIdentifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @Override
    public ResponseEntity<Void> deleteSubmodelDescriptorByIdThroughSuperpath( String aasIdentifier, String submodelIdentifier ) {
        shellService.deleteSubmodel(aasIdentifier, submodelIdentifier,getExternalSubjectIdOrEmpty( null ));
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
    // new todo: correct implementation
    public ResponseEntity<GetSubmodelDescriptorsResult> getAllSubmodelDescriptorsThroughSuperpath( String aasIdentifier, Integer limit, String cursor, @RequestHeader String externalSubjectId  ) {
        Shell savedShell = shellService.findShellByExternalId(aasIdentifier,getExternalSubjectIdOrEmpty(externalSubjectId));
        SubmodelCollectionDto dto = shellService.findAllSubmodel( limit,cursor, savedShell);
        GetSubmodelDescriptorsResult result= submodelMapper.toApiDto( dto );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById( String aasIdentifier, @RequestHeader String externalSubjectId ) {
            Shell saved = shellService.findShellByExternalId(aasIdentifier, getExternalSubjectIdOrEmpty(externalSubjectId));
           return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorByIdThroughSuperpath( String aasIdentifier, String submodelIdentifier ) {
        Submodel submodel = shellService.findSubmodelByExternalId(aasIdentifier, submodelIdentifier,getExternalSubjectIdOrEmpty( null ));
        return new ResponseEntity<>(submodelMapper.toApiDto(submodel), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor( AssetAdministrationShellDescriptor assetAdministrationShellDescriptor ) {
        Shell shell = shellMapper.fromApiDto(assetAdministrationShellDescriptor);
        shellService.mapShellCollection( shell );
        Shell saved = shellService.save(shell);
        return new ResponseEntity<>(shellMapper.toApiDto(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptorThroughSuperpath( String aasIdentifier, SubmodelDescriptor submodelDescriptor ) {
        Submodel toBeSaved = submodelMapper.fromApiDto(submodelDescriptor);
        toBeSaved.setIdExternal( submodelDescriptor.getId() );
        Submodel savedSubModel = shellService.save(aasIdentifier, toBeSaved, getExternalSubjectIdOrEmpty(null));
        return new ResponseEntity<>(submodelMapper.toApiDto(savedSubModel), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> putAssetAdministrationShellDescriptorById( String aasIdentifier, AssetAdministrationShellDescriptor assetAdministrationShellDescriptor ) {
        Shell shell = shellMapper.fromApiDto( assetAdministrationShellDescriptor );
        Shell shellFromDb = shellService.findShellByExternalId( aasIdentifier,getExternalSubjectIdOrEmpty(null) );
        shellService.update( shell.withId( shellFromDb.getId() ).withIdExternal(aasIdentifier  ),aasIdentifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> putSubmodelDescriptorByIdThroughSuperpath( String aasIdentifier, String submodelIdentifier, SubmodelDescriptor submodelDescriptor ) {
        Submodel submodel = submodelMapper.fromApiDto( submodelDescriptor );
        Submodel fromDB = shellService.findSubmodelByExternalId( aasIdentifier,submodelIdentifier,getExternalSubjectIdOrEmpty( null ) );
        shellService.deleteSubmodel(aasIdentifier,  submodelIdentifier,getExternalSubjectIdOrEmpty( null ));
        shellService.update( aasIdentifier, submodel.withIdExternal( submodelIdentifier ).withId( fromDB.getId() ) ,getExternalSubjectIdOrEmpty( "" ));
        return new ResponseEntity<>( HttpStatus.NO_CONTENT );
    }

    @Override
    public ResponseEntity<GetAllAssetAdministrationShellIdsByAssetLink200Response> getAllAssetAdministrationShellIdsByAssetLink(List<SpecificAssetId> assetIds,
    Integer limit, String cursor, @RequestHeader String externalSubjectId) {
        if (assetIds == null || assetIds.isEmpty()) {
            return new ResponseEntity<>(new GetAllAssetAdministrationShellIdsByAssetLink200Response(), HttpStatus.OK);
        }
        GetAllAssetAdministrationShellIdsByAssetLink200Response result  =
              shellService.findExternalShellIdsByIdentifiersByExactMatch(shellMapper.fromApiDto(assetIds), limit, cursor,getExternalSubjectIdOrEmpty(externalSubjectId));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
        public ResponseEntity<List<SpecificAssetId>> getAllAssetLinksById(String aasIdentifier,@RequestHeader String externalSubjectId) {
            Set<ShellIdentifier> identifiers = shellService.findShellIdentifiersByExternalShellId(aasIdentifier,getExternalSubjectIdOrEmpty(externalSubjectId));
            return new ResponseEntity<>(shellMapper.toApiDto(identifiers), HttpStatus.OK);
        }

        @Override
    public ResponseEntity<List<SpecificAssetId>> postAllAssetLinksById(String aasIdentifier, List<SpecificAssetId> specificAssetId) {
        Set<ShellIdentifier> shellIdentifiers = shellService.save(aasIdentifier, shellMapper.fromApiDto(specificAssetId),getExternalSubjectIdOrEmpty( null ));
        List<SpecificAssetId> list = shellMapper.toApiDto(shellIdentifiers);
        return new ResponseEntity<>(list, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<String>> postQueryAllAssetAdministrationShellIds(ShellLookup shellLookup,@RequestHeader String externalSubjectId) {
        List<SpecificAssetId> assetIds = shellLookup.getQuery().getAssetIds();
        List<String> externalIds = shellService.findExternalShellIdsByIdentifiersByAnyMatch(shellMapper.fromApiDto(assetIds),getExternalSubjectIdOrEmpty(externalSubjectId));
        return new ResponseEntity<>(externalIds, HttpStatus.OK);
    }

        private String getExternalSubjectIdOrEmpty(String externalSubjectId) {
            return (null ==externalSubjectId) ? "" : externalSubjectId;
        }
    }

