/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2016
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

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.opengis.fes._2.AbstractIdType;
import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.ResourceIdType;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

public class FilterHandler {
	// This class is just a dummy and needs
	// to be replaced by a FE filter support

	public Set<String> getResourceIds(FilterType filter, String handle) throws WFSException {
		Set<String> resourceIds = new HashSet<String>();

		if (filter.get_Id() != null) {
			for (JAXBElement<? extends AbstractIdType> abstractIdElement : filter.get_Id()) {
				if (!(abstractIdElement.getValue() instanceof ResourceIdType))
					throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Only " + new QName(Constants.FES_NAMESPACE_URI, "ResourceId").toString() + " is supported as ID filter.", handle);

				ResourceIdType resourceId = (ResourceIdType)abstractIdElement.getValue();
				if (!resourceId.isSetRid())
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The mandatory rid attribute is not provided on the " + new QName(Constants.FES_NAMESPACE_URI, "ResourceId").toString() + " element.", handle);

				if (resourceId.isSetVersion() ||
						resourceId.isSetStartDate() ||
						resourceId.isSetEndDate() ||
						resourceId.isSetPreviousRid())
					throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Feature versioning is not supported.", handle);

				resourceIds.add(resourceId.getRid().replaceAll("^#+", ""));
			}
		}

		return resourceIds;
	}
}
