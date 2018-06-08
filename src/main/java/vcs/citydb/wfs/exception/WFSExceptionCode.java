package vcs.citydb.wfs.exception;

import javax.servlet.http.HttpServletResponse;

public class WFSExceptionCode {
	private final String value;
	private final Integer httpStatusCode;
	
	// OWS 2.0 exception codes
	public static final WFSExceptionCode OPERATION_NOT_SUPPORTED = new WFSExceptionCode("OperationNotSupported", HttpServletResponse.SC_NOT_IMPLEMENTED);
	public static final WFSExceptionCode MISSING_PARAMETER_VALUE = new WFSExceptionCode("MissingParameterValue", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode INVALID_PARAMETER_VALUE = new WFSExceptionCode("InvalidParameterValue", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode VERSION_NEGOTIATION_FAILED = new WFSExceptionCode("VersionNegotiationFailed", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode INVALID_UPDATE_SEQUENCE = new WFSExceptionCode("InvalidUpdateSequence", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode OPTION_NOT_SUPPORTED = new WFSExceptionCode("OptionNotSupported", HttpServletResponse.SC_NOT_IMPLEMENTED);
	public static final WFSExceptionCode NO_APPLICABLE_CODE = new WFSExceptionCode("NoApplicableCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

	// WFS 2.0 exception codes
	public static final WFSExceptionCode CANNOT_LOCK_ALL_FEATURES = new WFSExceptionCode("CannotLockAllFeatures", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode DUPLICATE_STORED_QUERY_ID_VALUE = new WFSExceptionCode("DuplicateStoredQueryIdValue", HttpServletResponse.SC_CONFLICT);
	public static final WFSExceptionCode DUPLICATE_STORED_QUERY_PARAMETER_VALUE = new WFSExceptionCode("DuplicateStoredQueryParameterName", HttpServletResponse.SC_CONFLICT);
	public static final WFSExceptionCode FEATURES_NOT_LOCKED = new WFSExceptionCode("FeaturesNotLocked", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode INVALID_LOCK_ID = new WFSExceptionCode("InvalidLockId", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode INVALID_VALUE = new WFSExceptionCode("InvalidValue", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode LOCK_HAS_EXPIRED = new WFSExceptionCode("LockHasExpired", HttpServletResponse.SC_FORBIDDEN);
	public static final WFSExceptionCode OPERATION_PARSING_FAILED = new WFSExceptionCode("OperationParsingFailed", HttpServletResponse.SC_BAD_REQUEST);
	public static final WFSExceptionCode OPERATION_PROCESSING_FAILED = new WFSExceptionCode("OperationProcessingFailed", HttpServletResponse.SC_FORBIDDEN);
	public static final WFSExceptionCode RESPONSE_CACHE_EXPIRED = new WFSExceptionCode("ResponseCacheExpired", HttpServletResponse.SC_FORBIDDEN);
	public static final WFSExceptionCode NOT_FOUND = new WFSExceptionCode("NotFound", HttpServletResponse.SC_NOT_FOUND);
	
	// Server exception codes
	public static final WFSExceptionCode INTERNAL_SERVER_ERROR = new WFSExceptionCode("InternalServerError", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	public static final WFSExceptionCode SERVICE_UNAVAILABLE = new WFSExceptionCode("ServiceUnavailable", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
	
	public WFSExceptionCode(String value, Integer httpStatusCode) {
		this.value = value;
		this.httpStatusCode = httpStatusCode;
	}
	
	public WFSExceptionCode(String value) {
		this(value, null);
	}

	public String getValue() {
		return value;
	}

	public Integer getHttpStatusCode() {
		return httpStatusCode;
	}
	
}
