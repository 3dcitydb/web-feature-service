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
package vcs.citydb.wfs.config.capabilities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import vcs.citydb.wfs.config.Constants;
import net.opengis.ows._1.ServiceIdentification;
import net.opengis.ows._1.ServiceProvider;

@XmlType(name="OWSMetadataType", propOrder={
		"serviceIdentification",
		"serviceProvider"
})
public class OWSMetadata {
	@XmlElement(name="ServiceIdentification", namespace=Constants.OWS_NAMESPACE_URI)
	private ServiceIdentification serviceIdentification;
	@XmlElement(name="ServiceProvider", namespace=Constants.OWS_NAMESPACE_URI)
	private ServiceProvider serviceProvider;
	
	public ServiceIdentification getServiceIdentification() {
		return serviceIdentification;
	}
	
	public void setServiceIdentification(ServiceIdentification serviceIdentification) {
		this.serviceIdentification = serviceIdentification;
	}
	
	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}
	
	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
	
}
