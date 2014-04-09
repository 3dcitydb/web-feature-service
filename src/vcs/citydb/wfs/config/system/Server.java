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
package vcs.citydb.wfs.config.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ServiceType", propOrder={
		"externalServiceURL",
		"maxParallelRequests",
		"waitTimeout"
})
public class Server {
	@XmlElement(required=true)
	private String externalServiceURL = "";
	private Integer maxParallelRequests = 30;
	private Integer waitTimeout = 60;
	
	public String getExternalServiceURL() {
		return externalServiceURL;
	}

	public void setExternalServiceURL(String externalServiceURL) {
		this.externalServiceURL = externalServiceURL;
	}

	public int getMaxParallelRequests() {
		return maxParallelRequests;
	}
	
	public void setMaxParallelRequests(int maxParallelRequests) {
		if (maxParallelRequests <= 0)
			maxParallelRequests = 30;
		
		this.maxParallelRequests = maxParallelRequests;
	}
	
	public int getWaitTimeout() {
		return waitTimeout;
	}
	
	public void setWaitTimeout(int waitTimeout) {
		if (waitTimeout <= 0)
			waitTimeout = 60;
		
		this.waitTimeout = waitTimeout;
	}

}
