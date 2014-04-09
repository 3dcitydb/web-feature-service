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
package vcs.citydb.wfs.config.feature;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlType(name="CityGMLVersionType")
public class CityGMLVersionType {
	@XmlValue
	private CityGMLVersionEnum value;
	@XmlAttribute
	private Boolean isDefault;
	
	public CityGMLVersionEnum getValue() {
		return value;
	}

	public void setValue(CityGMLVersionEnum value) {
		this.value = value;
	}
	
	public Boolean isDefault() {
		if (isDefault != null)
			return isDefault.booleanValue();
		
		return false;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CityGMLVersionType && value == ((CityGMLVersionType)obj).value)
			return true;
		
		return super.equals(obj);
	}

	@XmlType(name="CityGMLVersion")
	@XmlEnum
	public static enum CityGMLVersionEnum {
		@XmlEnumValue("2.0")
		v2_0_0(CityGMLVersion.v2_0_0),
		@XmlEnumValue("1.0")
		v1_0_0(CityGMLVersion.v1_0_0);

		private final CityGMLVersion version;

		private CityGMLVersionEnum(CityGMLVersion version) {
			this.version = version;
		}

		public CityGMLVersion getCityGMLVersion() {
			return version;
		}

		public static CityGMLVersionEnum fromCityGMLVersion(CityGMLVersion version) {
			if (version == CityGMLVersion.v1_0_0)
				return v1_0_0;
			else
				return v2_0_0;
		}
	}
}
