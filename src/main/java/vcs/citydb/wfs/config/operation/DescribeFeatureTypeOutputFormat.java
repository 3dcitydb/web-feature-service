package vcs.citydb.wfs.config.operation;

public enum DescribeFeatureTypeOutputFormat {
	GML3_1("application/gml+xml; version=3.1"),
	CITY_JSON("application/json");
	
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

    @Override
    public String toString() {
        return value;
    }
}
