package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GetFeatureOutputFormatType")
@XmlEnum
public enum GetFeatureOutputFormat {
	@XmlEnumValue("application/gml+xml; version=3.1")
	GML3_1("application/gml+xml; version=3.1"),
	@XmlEnumValue("application/json")
	CITY_JSON("application/json");
	
	private final String value;

	GetFeatureOutputFormat(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static GetFeatureOutputFormat fromValue(String value) {
        for (GetFeatureOutputFormat c : GetFeatureOutputFormat.values()) {
            if (c.value.equals(value))
                return c;
        }

        return null;
    }
}
