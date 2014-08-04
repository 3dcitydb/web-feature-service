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
	
}
