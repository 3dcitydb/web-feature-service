package vcs.citydb.wfs.config.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumSet;

@XmlType(name = "ComparisonOperatorNameType")
@XmlEnum
public enum ComparisonOperatorName {
    @XmlEnumValue("PropertyIsEqualTo")
    PROPERTY_IS_EQUAL_TO("PropertyIsEqualTo"),
    @XmlEnumValue("PropertyIsNotEqualTo")
    PROPERTY_IS_NOT_EQUAL_TO("PropertyIsNotEqualTo"),
    @XmlEnumValue("PropertyIsLessThan")
    PROPERTY_IS_LESS_THAN("PropertyIsLessThan"),
    @XmlEnumValue("PropertyIsGreaterThan")
    PROPERTY_IS_GREATER_THAN("PropertyIsGreaterThan"),
    @XmlEnumValue("PropertyIsLessThanOrEqualTo")
    PROPERTY_IS_LESS_THAN_OR_EQUAL_TO("PropertyIsLessThanOrEqualTo"),
    @XmlEnumValue("PropertyIsGreaterThanOrEqualTo")
    PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO("PropertyIsGreaterThanOrEqualTo"),
    @XmlEnumValue("PropertyIsLike")
    PROPERTY_IS_LIKE("PropertyIsLike"),
    @XmlEnumValue("PropertyIsNull")
    PROPERTY_IS_NULL("PropertyIsNull"),
    @XmlEnumValue("PropertyIsNil")
    PROPERTY_IS_NIL("PropertyIsNil"),
    @XmlEnumValue("PropertyIsBetween")
    PROPERTY_IS_BETWEEN("PropertyIsBetween");

    public static final EnumSet<ComparisonOperatorName> MIN_STANDARD_FILTER = EnumSet.of(
            PROPERTY_IS_EQUAL_TO, PROPERTY_IS_NOT_EQUAL_TO, PROPERTY_IS_LESS_THAN, PROPERTY_IS_GREATER_THAN,
            PROPERTY_IS_LESS_THAN_OR_EQUAL_TO, PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO);

    public static final EnumSet<ComparisonOperatorName> STANDARD_FILTER = EnumSet.allOf(ComparisonOperatorName.class);

    private final String value;

    ComparisonOperatorName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ComparisonOperatorName fromValue(String value) {
        for (ComparisonOperatorName name : values())
            if (name.value.equals(value))
                return name;

        return null;
    }

    @Override
    public String toString() {
        return value;
    }

}
