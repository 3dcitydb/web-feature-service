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
