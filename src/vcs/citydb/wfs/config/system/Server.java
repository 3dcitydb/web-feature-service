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
package vcs.citydb.wfs.config.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ServiceType", propOrder={
		"externalServiceURL",
		"maxParallelRequests",
		"waitTimeout",
		"enableCORS"
})
public class Server {
	@XmlElement(required=true)
	private String externalServiceURL = "";
	private Integer maxParallelRequests = 30;
	private Integer waitTimeout = 60;
	private Boolean enableCORS = true;
	
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
	
	public boolean isEnableCORS() {
		return enableCORS;
	}

	public void setEnableCORS(boolean enableCORS) {
		this.enableCORS = enableCORS;
	}

}
