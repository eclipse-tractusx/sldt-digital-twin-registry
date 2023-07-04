package org.eclipse.tractusx.semantics.registry.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;

import lombok.Value;
import lombok.With;

@Value
@With
public class Reference {

   @Id
   UUID id;

   private String type;

   @MappedCollection(idColumn = "fk_reference_id")
   private List<ReferenceKey> keys = null;

   @Column("referred_semantic_id" )
   private ReferenceParent referredSemanticId;

}
