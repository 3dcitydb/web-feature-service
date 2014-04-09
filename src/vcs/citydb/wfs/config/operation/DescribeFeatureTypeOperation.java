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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DescribeFeatureTypeOperationType", propOrder={
		"outputFormat"
})
public class DescribeFeatureTypeOperation {
	@XmlElement(nillable=false)
	private LinkedHashSet<DescribeFeatureTypeOutputFormat> outputFormat;
	
	public DescribeFeatureTypeOperation() {
		outputFormat = new LinkedHashSet<>();
		outputFormat.add(DescribeFeatureTypeOutputFormat.GML3_1);
	}

	public LinkedHashSet<DescribeFeatureTypeOutputFormat> getOutputFormat() {
		return outputFormat;
	}
	
	public List<String> getOutputFormatAsString() {
		List<String> formats = new ArrayList<>();
		for (DescribeFeatureTypeOutputFormat format : outputFormat)
			formats.add(format.value());
		
		return formats;
	}

	public void setOutputFormat(LinkedHashSet<DescribeFeatureTypeOutputFormat> outputFormat) {
		this.outputFormat = outputFormat;
	}
	
	public boolean supportsOutputFormat(DescribeFeatureTypeOutputFormat outputFormat) {
		return this.outputFormat.contains(outputFormat);
	}
	
	public boolean supportsOutputFormat(String outputFormat) {
		return this.outputFormat.contains(DescribeFeatureTypeOutputFormat.fromValue(outputFormat));
	}
	
}
