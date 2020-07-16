package vcs.citydb.wfs.config.operation;

public enum GetFeatureOutputFormat {
	GML3_1("application/gml+xml; version=3.1"),
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

    @Override
    public String toString() {
        return value;
    }
}
