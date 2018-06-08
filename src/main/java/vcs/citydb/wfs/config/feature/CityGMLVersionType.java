package vcs.citydb.wfs.config.feature;

import java.util.Objects;

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
	
	public boolean isDefault() {
		return isDefault != null ? isDefault.booleanValue() : false;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
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
