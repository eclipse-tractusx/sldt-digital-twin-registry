package org.eclipse.tractusx.semantics.registry.model;

public enum SecurityType {

   NONE("NONE"),

   RFC_TLSA("RFC_TLSA"),

   W3C_DID("W3C_DID");

   private String value;

   SecurityType (String value){
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
