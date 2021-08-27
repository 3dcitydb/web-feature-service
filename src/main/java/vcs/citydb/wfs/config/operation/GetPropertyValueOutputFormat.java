package vcs.citydb.wfs.config.operation;

public enum GetPropertyValueOutputFormat {
	GML3_1("application/gml+xml; version=3.1");
	
	private final String value;

	GetPropertyValueOutputFormat(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static GetPropertyValueOutputFormat fromValue(String value) {
        for (GetPropertyValueOutputFormat c : GetPropertyValueOutputFormat.values()) {
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
