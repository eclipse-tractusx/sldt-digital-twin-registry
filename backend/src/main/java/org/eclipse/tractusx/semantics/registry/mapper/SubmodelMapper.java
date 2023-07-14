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
package org.eclipse.tractusx.semantics.registry.mapper;



import org.eclipse.tractusx.semantics.aas.registry.model.Endpoint;
import org.eclipse.tractusx.semantics.aas.registry.model.GetSubmodelDescriptorsResult;
import org.eclipse.tractusx.semantics.aas.registry.model.Key;
import org.eclipse.tractusx.semantics.aas.registry.model.KeyTypes;
import org.eclipse.tractusx.semantics.aas.registry.model.LangStringTextType;
import org.eclipse.tractusx.semantics.aas.registry.model.Reference;
import org.eclipse.tractusx.semantics.aas.registry.model.ReferenceTypes;
import org.eclipse.tractusx.semantics.aas.registry.model.SubmodelDescriptor;
import org.eclipse.tractusx.semantics.registry.dto.SubmodelCollectionDto;
import org.eclipse.tractusx.semantics.registry.model.Submodel;
import org.eclipse.tractusx.semantics.registry.model.SubmodelDescription;
import org.eclipse.tractusx.semantics.registry.model.SubmodelEndpoint;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubmodelMapper {
    @Mappings({
            @Mapping(target="idExternal", source="id"),
            @Mapping(target = "descriptions", source = "description"),
            @Mapping(target="semanticId", source = "semanticId"),
            @Mapping(target = "id", ignore = true)
    })
    Submodel fromApiDto(SubmodelDescriptor apiDto);

   SubmodelDescription mapShellDescription (LangStringTextType description);

    @Mappings({
            @Mapping(target="interfaceName", source = "interface"),
            @Mapping(target="endpointAddress", source = "protocolInformation.href"),
            @Mapping(target="endpointProtocol", source = "protocolInformation.endpointProtocol"),
            @Mapping(target="subProtocol", source = "protocolInformation.subprotocol"),
            @Mapping(target="subProtocolBody", source = "protocolInformation.subprotocolBody"),
            @Mapping(target="subProtocolBodyEncoding", source = "protocolInformation.subprotocolBodyEncoding"),
            @Mapping(target = "endpointProtocolVersion", source = "protocolInformation.endpointProtocolVersion" , qualifiedByName = "endpointProtocolVersionMapping"),
    })
    SubmodelEndpoint fromApiDto(Endpoint apiDto);

    //TODO:Change the data base column to List of String
    @Named("endpointProtocolVersionMapping")
    default String endpointProtocolVersion(List<String> endpointProtocolVersions) {
       return Optional.ofNullable(endpointProtocolVersions).map(endpointPVs -> String.join(",", endpointPVs)).orElse(null);
    }

    @Named("protocolVersionDescriptor")
    default List<String>  protocolVersionDescriptor(String version){
        List<String> versions= Stream.of(version.split(","))
              .map(String::trim)
                .collect( Collectors.toList());
        return versions;
    }

   @Mappings({
         @Mapping(source = "items", target = "result"),
         @Mapping(source = "cursor", target = "pagingMetadata.cursor"),
   })
   GetSubmodelDescriptorsResult toApiDto( SubmodelCollectionDto shell);

    @Mappings({
            @Mapping(source = "endpointProtocolVersion", target = "protocolInformation.endpointProtocolVersion" , qualifiedByName = "protocolVersionDescriptor"),
    })
    @InheritInverseConfiguration
    Endpoint toApiDto(SubmodelEndpoint apiDto);

    @InheritInverseConfiguration
    SubmodelDescriptor toApiDto(Submodel shell);

   LangStringTextType mapSubModelDescription (SubmodelDescription description);

   @InheritInverseConfiguration
   List<SubmodelDescriptor> toApiDto( Set<Submodel> submodels );

   default String map(Reference reference) {
      return Optional.ofNullable(reference).map(Reference::getKeys)
            .map( Collection::stream)
            .orElseGet( Stream::empty)
            .map(Key::getValue)
            .filter( Objects::nonNull)
            .findFirst()
            .orElse(null);
   }

   // todo: implement types
    default Reference map(String semanticId){
        if(semanticId == null ||  semanticId.isBlank()) {
            return null;
        }
        Reference reference = new Reference();
        reference.setType( ReferenceTypes.EXTERNALREFERENCE );
        Key key = new Key();
        key.setType( KeyTypes.SUBMODEL );
        key.setValue( semanticId );
        reference.setKeys( List.of(key) );
        return reference;
    }

   @AfterMapping
   default Submodel mapSemanticIds( SubmodelDescriptor apiDto, @MappingTarget Submodel submodel){
      if(apiDto.getSemanticId().getKeys()!=null){
         return submodel.withSemanticId( apiDto.getSemanticId().getKeys().get( 0 ).getValue() );
      }else{
         return submodel.withSemanticId( "" );
      }
   }

}
