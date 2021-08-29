package vcs.citydb.wfs.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
	public static final String DEFAULT_OWS_TITLE = "3DCityDB Web Feature Service";
	public static final long UNKNOWN_NUMBER_MATCHED = Long.MAX_VALUE;
	public static final long COUNT_DEFAULT = Long.MAX_VALUE;

	public static final String CONFIG_FILE;
	public static final String CONFIG_SCHEMA_PATH = "src/main/webapp/WEB-INF/schemas/config";
	public static final String CONFIG_SCHEMA_FILE = CONFIG_SCHEMA_PATH + "/config.xsd";
	public static final String STORED_QUERIES_PATH = "/WEB-INF/stored-queries";
	public static final String ADE_EXTENSIONS_PATH;
	public static final String XSLT_STYLESHEETS_PATH = "/WEB-INF/xslt-stylesheets";
	public static final String SCHEMAS_PATH = "/WEB-INF/schemas";
	public static final String CITYGML_1_0_SCHEMAS_PATH = SCHEMAS_PATH + "/ogc/citygml/1.0.0";
	public static final String CITYGML_2_0_SCHEMAS_PATH = SCHEMAS_PATH + "/ogc/citygml/2.0.0";
	public static final String CITYJSON_SCHEMA_PATH = SCHEMAS_PATH + "/cityjson";
	public static final String LOG_PATH = "/WEB-INF";
	public static final String LOG_FILE = "wfs.log";

	public static final String INIT_ERROR_ATTRNAME = "init_error";
	
	public static final String WFS_SERVICE_PATH = "/wfs";
	public static final String WFS_SERVICE_STRING = "WFS";
	public static final List<String> SUPPORTED_WFS_VERSIONS = new ArrayList<>(Arrays.asList("2.0.2", "2.0.0"));
	public static String DEFAULT_WFS_VERSION = "2.0.2";
	
	public static final String WFS_NAMESPACE_URI = "http://www.opengis.net/wfs/2.0";
	public static final String WFS_NAMESPACE_PREFIX = "wfs";
	public static final String WFS_SCHEMA_LOCATION = "http://schemas.opengis.net/wfs/2.0/wfs.xsd";
	public static final String FES_NAMESPACE_URI = "http://www.opengis.net/fes/2.0";
	public static final String FES_NAMESPACE_PREFIX = "fes";
	public static final String FES_SCHEMA_LOCATION = "http://schemas.opengis.net/filter/2.0/filterAll.xsd";
	public static final String OWS_NAMESPACE_URI = "http://www.opengis.net/ows/1.1";
	public static final String OWS_NAMESPACE_PREFIX = "ows";
	public static final String OWS_SCHEMA_LOCATION = "http://schemas.opengis.net/ows/1.1.0/owsAll.xsd";
	public static final String GML_3_2_1_NAMESPACE_URI = "http://www.opengis.net/gml/3.2";
	public static final String GML_3_3_NAMESPACE_URI = "http://www.opengis.net/gml/3.3";

	public static final String TEXTURE_SERVICE_PATH = "/texture";
	public static final String TEXTURE_CACHE_PATH = "/WEB-INF/texture_cache";
	public static final int TEXTURE_CACHE_BUCKETS = 1000;

	public static final String MANAGEMENT_SERVICE_PATH = "/service";

	static {
		if (System.getenv("WFS_CONFIG_FILE") != null) {
			CONFIG_FILE = System.getenv("WFS_CONFIG_FILE");
		} else if (System.getenv("VC_WFS_CONFIG_FILE") != null) {
			// VC_WFS_CONFIG_FILE is deprecated but still supported for backwards compatibility
			CONFIG_FILE = System.getenv("VC_WFS_CONFIG_FILE");
		} else {
			CONFIG_FILE = "WEB-INF/config.xml";
		}

		if (System.getenv("WFS_ADE_EXTENSIONS_PATH") != null) {
			ADE_EXTENSIONS_PATH = System.getenv("WFS_ADE_EXTENSIONS_PATH");
		} else if (System.getenv("VC_WFS_ADE_EXTENSIONS_PATH") != null) {
			// VC_WFS_ADE_EXTENSIONS_PATH is deprecated but still supported for backwards compatibility
			ADE_EXTENSIONS_PATH = System.getenv("VC_WFS_ADE_EXTENSIONS_PATH");
		} else {
			ADE_EXTENSIONS_PATH = "WEB-INF/ade-extensions";
		}
	}
}