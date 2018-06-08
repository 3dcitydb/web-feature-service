package vcs.citydb.wfs.kvp;

import java.util.Arrays;
import java.util.List;

public class KVPConstants {
	public static final String ITEM_DELIMITER = ",";
	public static final String LIST_DELIMITER = "\\)\\(";
	public static final String DEFAULT_FILTER_LANGUAGE = "urn:ogc:def:queryLanguage:OGC-FES:Filter";
	
	public static final String GET_CAPABILITIES = "GetCapabilities";
	public static final String DESCRIBE_FEATURE_TYPE = "DescribeFeatureType";
	public static final String GET_FEATURE = "GetFeature";
	public static final String LIST_STORED_QUERIES = "ListStoredQueries";
	public static final String DESCRIBE_STORED_QUERIES = "DescribeStoredQueries";

	public static final String REQUEST = "REQUEST";
	public static final String SERVICE = "SERVICE";
	public static final String VERSION = "VERSION";
	public static final String START_INDEX = "STARTINDEX";
	public static final String COUNT = "COUNT";
	public static final String OUTPUT_FORMAT = "OUTPUTFORMAT";
	public static final String RESULT_TYPE = "RESULTTYPE";
	public static final String RESOLVE = "RESOLVE";
	public static final String RESOLVE_DEPTH = "RESOLVEDEPTH";
	public static final String RESOLVE_TIMEOUT = "RESOLVETIMEOUT";
	public static final String RESOLVE_PATH = "RESOLVEPATH";
	public static final String TYPE_NAME = "TYPENAME";
	public static final String TYPE_NAMES = "TYPENAMES";
	public static final String ALIASES = "ALIASES";
	public static final String RESOURCE_ID = "RESOURCEID";
	public static final String SRS_NAME = "SRSNAME";
	public static final String PROPERTY_NAME = "PROPERTYNAME";
	public static final String FILTER = "FILTER";
	public static final String FILTER_LANGUAGE = "FILTER_LANGUAGE";
	public static final String BBOX = "BBOX";
	public static final String SORT_BY = "SORTBY";
	public static final String STOREDQUERY_ID = "STOREDQUERY_ID";
	public static final String VALUE_REFERENCE = "VALUEREFERENCE";
	public static final String ACCEPT_VERSIONS = "ACCEPTVERSIONS";
	public static final String NAMESPACES = "NAMESPACES";
	public static final String LOCK_ID = "LOCKID";
	public static final String EXPIRY = "EXPIRY";
	public static final String LOCK_ACTION = "LOCKACTION";
	
	public static final List<String> PARAMETERS = Arrays.asList(REQUEST, SERVICE, VERSION, START_INDEX, COUNT, OUTPUT_FORMAT, RESULT_TYPE,
			RESOLVE, RESOLVE_DEPTH, RESOLVE_TIMEOUT, RESOLVE_PATH, TYPE_NAME, TYPE_NAMES, ALIASES, RESOURCE_ID, SRS_NAME, PROPERTY_NAME, FILTER,
			FILTER_LANGUAGE, BBOX, SORT_BY, STOREDQUERY_ID, VALUE_REFERENCE, ACCEPT_VERSIONS, NAMESPACES, LOCK_ID, EXPIRY, LOCK_ACTION);
}
