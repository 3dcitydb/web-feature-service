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
