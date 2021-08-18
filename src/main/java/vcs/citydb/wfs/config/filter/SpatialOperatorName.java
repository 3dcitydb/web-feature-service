package vcs.citydb.wfs.config.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumSet;

@XmlType(name="SpatialOperatorNameType")
@XmlEnum
public enum SpatialOperatorName {
	@XmlEnumValue("Equals")
	EQUALS("Equals"),
	@XmlEnumValue("Disjoint")
	DISJOINT("Disjoint"),
	@XmlEnumValue("Touches")
	TOUCHES("Touches"),
	@XmlEnumValue("Within")
	WITHIN("Within"),
	@XmlEnumValue("Overlaps")
	OVERLAPS("Overlaps"),
	@XmlEnumValue("Intersects")
	INTERSECTS("Intersects"),
	@XmlEnumValue("Contains")
	CONTAINS("Contains"),
	@XmlEnumValue("DWithin")
	DWITHIN("DWithin"),
	@XmlEnumValue("Beyond")
	BEYOND("Beyond"),
	@XmlEnumValue("BBOX")
	BBOX("BBOX");
	
	public static final EnumSet<SpatialOperatorName> MIN_SPATIAL_FILTER = EnumSet.of(BBOX);
	
	private final String value;

	private SpatialOperatorName(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static SpatialOperatorName fromValue(String value) {
		for (SpatialOperatorName name : values())
			if (name.value.equals(value))
				return name;

		return null;
	}

	@Override
	public String toString() {
		return value;
	}
	
}
