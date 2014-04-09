/*
 * This file is part of the 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright (c) 2014
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 * 
 * The 3D City Database Web Feature Service is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
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
