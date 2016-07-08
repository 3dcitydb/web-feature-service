/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2016
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
