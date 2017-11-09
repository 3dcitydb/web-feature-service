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
package vcs.citydb.wfs.config.security;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SecurityType", propOrder={
		"maxFeatureCount",
		"stripGeometry"
})
public class Security {
	private Long maxFeatureCount = Long.MAX_VALUE;
	@XmlElement(defaultValue="false")
	private Boolean stripGeometry = false;

	public long getMaxFeatureCount() {
		return maxFeatureCount;
	}

	public void setMaxFeatureCount(long maxFeatureCount) {
		this.maxFeatureCount = maxFeatureCount;
	}
	
	public boolean isSetMaxFeatureCount() {
		return maxFeatureCount != Long.MAX_VALUE;
	}

	public boolean isStripGeometry() {		
		return stripGeometry;
	}

	public void setStripGeometry(boolean stripGeometry) {
		this.stripGeometry = stripGeometry;
	}
	
}
