/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vcs.citydb.wfs.operation.storedquery;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

public enum XSDataType {
	XS_STRING("string"),
	XS_INTEGER("integer"),
	XS_NONNEGATIVE_INTEGER("nonNegativeInteger"),
	XS_NONPOSITIVE_INTEGER("nonPositiveInteger"),
	XS_INT("int"),
	XS_LONG("long"),
	XS_SHORT("short"),
	XS_DECIMAL("decimal"),
	XS_FLOAT("float"),
	XS_DOUBLE("double"),
	XS_BOOLEAN("boolean"),
	XS_BYTE("byte"),
	XSAD_QNAME("QName"),
	XS_DATETIME("dateTime"),
	XS_BASE64BINARY("base64Binary"),
	XS_HEXBINARY("hexBinary"),
	XS_UNSIGNED_INT("unsignedInt"),
	XS_UNSIGNED_SHORT("unsignedShort"),
	XS_UNSIGNED_BYTE("unsignedByte"),
	XS_UNSIGNED_LONG("unsignedLong"),
	XS_ANY_URI("anyURI"),
	XS_TIME("time"),
	XS_DATE("date"),
	XS_GDAY("gDay"),
	XS_GMONTH("gMonth"),
	XS_GYEAR("gYear"),
	XS_DURATION("duration");
	
	private final String localPart;
	
	private XSDataType(String localName) {
		this.localPart = localName;
	}
	
	public QName getName() {
		return new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, localPart);
	}
	
}
