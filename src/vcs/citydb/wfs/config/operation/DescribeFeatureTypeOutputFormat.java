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
package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DescribeFeatureTypeOutputFormatType")
@XmlEnum
public enum DescribeFeatureTypeOutputFormat {
	@XmlEnumValue("application/gml+xml; version=3.1")
	GML3_1("application/gml+xml; version=3.1");
	
	private final String value;

	DescribeFeatureTypeOutputFormat(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static DescribeFeatureTypeOutputFormat fromValue(String value) {
        for (DescribeFeatureTypeOutputFormat c : DescribeFeatureTypeOutputFormat.values()) {
            if (c.value.equals(value))
                return c;
        }

        return null;
    }
}
