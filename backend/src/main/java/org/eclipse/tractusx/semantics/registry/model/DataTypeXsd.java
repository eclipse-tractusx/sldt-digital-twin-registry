package org.eclipse.tractusx.semantics.registry.model;

public enum DataTypeXsd {
   ANYURI("xs:anyURI"),

   BASE64BINARY("xs:base64Binary"),

   BOOLEAN("xs:boolean"),

   BYTE("xs:byte"),

   DATE("xs:date"),

   DATETIME("xs:dateTime"),

   DECIMAL("xs:decimal"),

   DOUBLE("xs:double"),

   DURATION("xs:duration"),

   FLOAT("xs:float"),

   GDAY("xs:gDay"),

   GMONTH("xs:gMonth"),

   GMONTHDAY("xs:gMonthDay"),

   GYEAR("xs:gYear"),

   GYEARMONTH("xs:gYearMonth"),

   HEXBINARY("xs:hexBinary"),

   INT("xs:int"),

   INTEGER("xs:integer"),

   LONG("xs:long"),

   NEGATIVEINTEGER("xs:negativeInteger"),

   NONNEGATIVEINTEGER("xs:nonNegativeInteger"),

   NONPOSITIVEINTEGER("xs:nonPositiveInteger"),

   POSITIVEINTEGER("xs:positiveInteger"),

   SHORT("xs:short"),

   STRING("xs:string"),

   TIME("xs:time"),

   UNSIGNEDBYTE("xs:unsignedByte"),

   UNSIGNEDINT("xs:unsignedInt"),

   UNSIGNEDLONG("xs:unsignedLong"),

   UNSIGNEDSHORT("xs:unsignedShort");

   private String value;

   DataTypeXsd(String value) {
      this.value = value;
   }

}
