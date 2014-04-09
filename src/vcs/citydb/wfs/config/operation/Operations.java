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
package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OperationsType", propOrder={
		"useXMLValidation",
		"getFeature",
		"describeFeatureType"
})
public class Operations {
	private Boolean useXMLValidation = true;
	@XmlElement(name="GetFeature")
    private GetFeatureOperation getFeature;
	@XmlElement(name="DescribeFeatureType")
	private DescribeFeatureTypeOperation describeFeatureType;
	
	public Operations() {
		getFeature = new GetFeatureOperation();
		describeFeatureType = new DescribeFeatureTypeOperation();
	}
	
	public Boolean isUseXMLValidation() {
		return useXMLValidation;
	}

	public void setUseXMLValidation(boolean useXMLValidation) {
		this.useXMLValidation = useXMLValidation;
	}

	public GetFeatureOperation getGetFeature() {
		return getFeature;
	}

	public void setGetFeature(GetFeatureOperation getFeature) {
		this.getFeature = getFeature;
	}

	public DescribeFeatureTypeOperation getDescribeFeatureType() {
		return describeFeatureType;
	}

	public void setDescribeFeatureType(DescribeFeatureTypeOperation describeFeatureType) {
		this.describeFeatureType = describeFeatureType;
	}
	
}
