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
package vcs.citydb.wfs.operation.getfeature;

import java.util.Set;

import javax.xml.namespace.QName;

import org.citydb.modules.common.filter.feature.FeatureClassFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;

public class QueryExpression {
	// This class is just a dummy and needs
	// to be replaced by a FE object layer
	private Set<QName> featureTypeNames;
	private String handle;
	private boolean isGetFeatureById;
	
	// TODO: replace these filter classes
	private GmlIdFilter gmlIdFilter;
	private FeatureClassFilter featureTypeFilter;
	
	public Set<QName> getFeatureTypeNames() {
		return featureTypeNames;
	}

	public void setFeatureTypeNames(Set<QName> featureTypeNames) {
		this.featureTypeNames = featureTypeNames;
	}
	
	public GmlIdFilter getGmlIdFilter() {
		return gmlIdFilter;
	}

	public void setGmlIdFilter(GmlIdFilter gmlIdFilter) {
		this.gmlIdFilter = gmlIdFilter;
	}

	public FeatureClassFilter getFeatureTypeFilter() {
		return featureTypeFilter;
	}

	public void setFeatureTypeFilter(FeatureClassFilter featureTypeFilter) {
		this.featureTypeFilter = featureTypeFilter;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}
	public boolean isGetFeatureById() {
		return isGetFeatureById;
	}
	public void setGetFeatureById(boolean isGetFeatureById) {
		this.isGetFeatureById = isGetFeatureById;
	}
	
}
