/*******************************************************************************
 * Copyright (c) 2023 Robert Bosch Manufacturing Solutions GmbH and others
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
   DataTypeXsd(String value) {this.value = value;}
   public String getValue() {return value;}
   @Override
   public String toString() {return String.valueOf(value);}
}
