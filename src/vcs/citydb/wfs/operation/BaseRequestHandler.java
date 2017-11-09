/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vcs.citydb.wfs.operation;

import org.citydb.util.Util;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import net.opengis.wfs._2.BaseRequestType;

public class BaseRequestHandler {

	public void validate(BaseRequestType wfsRequest) throws WFSException {
		final String operationHandle = wfsRequest.getHandle();
		
		// check service attribute
		if (!wfsRequest.isSetService() || !Constants.WFS_SERVICE_STRING.equals(wfsRequest.getService()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'service' must match the fixed value '" + Constants.WFS_SERVICE_STRING + "'.", operationHandle);

		// check version attribute
		if (!wfsRequest.isSetVersion() || !Constants.SUPPORTED_WFS_VERSIONS.contains(wfsRequest.getVersion()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'version' must match one of the supported version numbers '" + Util.collection2string(Constants.SUPPORTED_WFS_VERSIONS, ", ") + "'.", operationHandle);
	}
	
}
