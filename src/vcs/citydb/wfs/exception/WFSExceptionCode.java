/*
 * This file is part of the 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright (c) 2014
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 * 
 * The 3D City Database Web Feature Service is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
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
