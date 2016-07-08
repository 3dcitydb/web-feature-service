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
package vcs.citydb.wfs.config.feature;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlType(name="FeatureTypesType", propOrder={
		"featureTypes",
		"versions"
})
public class FeatureTypes {
	@XmlElement(name="featureType", nillable=false, required=true)
	private LinkedHashSet<FeatureType> featureTypes;
	@XmlElement(name="version", nillable=false, required=true)
	private LinkedHashSet<CityGMLVersionType> versions;

	@XmlTransient
	private List<QName> advertisedFeatureTypes;

	public FeatureTypes() {
		featureTypes = new LinkedHashSet<>();
		versions = new LinkedHashSet<>();
	}

	public Set<FeatureType> getFeatureTypes() {
		return featureTypes;
	}

	public void setFeatureTypes(LinkedHashSet<FeatureType> featureTypes) {
		this.featureTypes = featureTypes;
	}

	public CityGMLVersion getDefaultVersion() {
		for (CityGMLVersionType tmp : versions)
			if (tmp.isDefault())
				return tmp.getValue().getCityGMLVersion();

		if (versions.size() == 1)
			return versions.iterator().next().getValue().getCityGMLVersion();

		return CityGMLVersion.DEFAULT;
	}

	public List<CityGMLVersion> getVersions() {
		List<CityGMLVersion> result = new ArrayList<>();
		for (CityGMLVersionType tmp : versions) {
			if (tmp.getValue() == null)
				continue;
			
			result.add(tmp.getValue().getCityGMLVersion());
		}

		return result;
	}

	public List<QName> getAdvertisedFeatureTypes() {
		if (advertisedFeatureTypes == null) {
			advertisedFeatureTypes = new ArrayList<>();

			for (FeatureType type : featureTypes) {				
				for (CityGMLVersion version : getVersions()) {				
					QName qName = type.getQName(version);
					if (qName != null)
						advertisedFeatureTypes.add(qName);
				}
			}
		}

		return advertisedFeatureTypes;
	}

	public List<QName> getDefaultFeatureTypes() {
		List<QName> result = new ArrayList<>();
		CityGMLVersion version = getDefaultVersion();

		for (FeatureType type : featureTypes) {				
			QName qName = type.getQName(version);
			if (qName != null)
				result.add(qName);
		}

		return result;
	}

	public List<CityGMLModule> getCityGMLModules() {
		List<CityGMLModule> result = new ArrayList<>();
		for (QName name : getAdvertisedFeatureTypes())
			result.add(Modules.getCityGMLModule(name.getNamespaceURI()));

		return result;
	}

}
