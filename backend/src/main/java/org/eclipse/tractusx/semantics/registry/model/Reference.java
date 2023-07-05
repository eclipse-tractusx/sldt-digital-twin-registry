package org.eclipse.tractusx.semantics.registry.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;

import lombok.Value;

@Value

public class Reference {

   @Id
   UUID id;
   String type;

   @MappedCollection(idColumn = "fk_reference_id")
   List<ReferenceKey> keys;

   @Column("fk_referred_semantic_id" )
   ReferenceParent referredSemanticId;

}
