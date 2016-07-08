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
