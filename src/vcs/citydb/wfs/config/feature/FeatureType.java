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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import net.opengis.ows._1.WGS84BoundingBoxType;

import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import vcs.citydb.wfs.config.Constants;

@XmlType(name="FeatureTypeType", propOrder={
		"name",
		"wgs84BoundingBox"
})
public class FeatureType {
	@XmlElement(required=true)
	private FeatureTypeName name;
	@XmlElement(name="WGS84BoundingBox", namespace=Constants.OWS_NAMESPACE_URI)
	private WGS84BoundingBoxType wgs84BoundingBox;
	
	public FeatureTypeName getName() {
		return name;
	}
	
	public void setName(FeatureTypeName name) {
		this.name = name;
	}
	
	public WGS84BoundingBoxType getWGS84BoundingBox() {
		return wgs84BoundingBox;
	}
	
	public void setWGS84BoundingBox(WGS84BoundingBoxType wgs84BoundingBox) {
		this.wgs84BoundingBox = wgs84BoundingBox;
	}

	public QName getQName(CityGMLVersion version) {
		if (name == null)
			return null;
		
		QName qName = null;
		
		switch (name) {
		case BRIDGE:
			if (version == CityGMLVersion.v2_0_0)
				qName = new QName(version.getModule(CityGMLModuleType.BRIDGE).getNamespaceURI(), "Bridge");
			break;
		case BUILDING:
			qName = new QName(version.getModule(CityGMLModuleType.BUILDING).getNamespaceURI(), "Building");
			break;
		case CITY_FURNITURE:
			qName = new QName(version.getModule(CityGMLModuleType.CITY_FURNITURE).getNamespaceURI(), "CityFurniture");
			break;
		case CITY_OBJECT_GROUP:
			qName = new QName(version.getModule(CityGMLModuleType.CITY_OBJECT_GROUP).getNamespaceURI(), "CityObjectGroup");
			break;
		case GENERIC_CITY_OBJECT:
			qName = new QName(version.getModule(CityGMLModuleType.GENERICS).getNamespaceURI(), "GenericCityObject");
			break;
		case LAND_USE:
			qName = new QName(version.getModule(CityGMLModuleType.LAND_USE).getNamespaceURI(), "LandUse");
			break;
		case PLANT_COVER:
			qName = new QName(version.getModule(CityGMLModuleType.VEGETATION).getNamespaceURI(), "PlantCover");
			break;
		case RAILWAY:
			qName = new QName(version.getModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI(), "Railway");
			break;
		case RELIEF_FEATURE:
			qName = new QName(version.getModule(CityGMLModuleType.RELIEF).getNamespaceURI(), "ReliefFeature");
			break;
		case ROAD:
			qName = new QName(version.getModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI(), "Road");
			break;
		case SOLITARY_VEGETATION_OBJECT:
			qName = new QName(version.getModule(CityGMLModuleType.VEGETATION).getNamespaceURI(), "SolitaryVegetationObject");
			break;
		case SQUARE:
			qName = new QName(version.getModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI(), "Square");
			break;
		case TRACK:
			qName = new QName(version.getModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI(), "Track");
			break;
		case TRANSPORTATION_COMPLEX:
			qName = new QName(version.getModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI(), "TransportationComplex");
			break;
		case TUNNEL:
			if (version == CityGMLVersion.v2_0_0)
				qName = new QName(version.getModule(CityGMLModuleType.TUNNEL).getNamespaceURI(), "Tunnel");
			break;
		case WATER_BODY:
			qName = new QName(version.getModule(CityGMLModuleType.WATER_BODY).getNamespaceURI(), "WaterBody");
			break;
		}
		
		return qName;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FeatureType && name == ((FeatureType)obj).name)
			return true;
		
		return super.equals(obj);
	}

}
