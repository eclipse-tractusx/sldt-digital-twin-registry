package org.eclipse.tractusx.semantics.registry.model;

public enum ReferenceKeyType {

   ANNOTATEDRELATIONSHIPELEMENT("AnnotatedRelationshipElement"),

   ASSETADMINISTRATIONSHELL("AssetAdministrationShell"),

   BASICEVENTELEMENT("BasicEventElement"),

   BLOB("Blob"),

   CAPABILITY("Capability"),

   CONCEPTDESCRIPTION("ConceptDescription"),

   DATAELEMENT("DataElement"),

   ENTITY("Entity"),

   EVENTELEMENT("EventElement"),

   FILE("File"),

   FRAGMENTREFERENCE("FragmentReference"),

   GLOBALREFERENCE("GlobalReference"),

   IDENTIFIABLE("Identifiable"),

   MULTILANGUAGEPROPERTY("MultiLanguageProperty"),

   OPERATION("Operation"),

   PROPERTY("Property"),

   RANGE("Range"),

   REFERABLE("Referable"),

   REFERENCEELEMENT("ReferenceElement"),

   RELATIONSHIPELEMENT("RelationshipElement"),

   SUBMODEL("Submodel"),

   SUBMODELELEMENT("SubmodelElement"),

   SUBMODELELEMENTCOLLECTION("SubmodelElementCollection"),

   SUBMODELELEMENTLIST("SubmodelElementList");

   private String value;

   ReferenceKeyType(String value) {
      this.value = value;
   }

   public String getValue() {
      return value;
   }

   @Override
   public String toString() {
      return String.valueOf(value);
   }

}
