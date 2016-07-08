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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.model.citygml.CityGMLClass;

@XmlType(name="FeatureTypeNameType")
@XmlEnum
public enum FeatureTypeName {
	@XmlEnumValue("Building")
	BUILDING(CityGMLClass.BUILDING),
	@XmlEnumValue("Bridge")
	BRIDGE(CityGMLClass.BRIDGE),
	@XmlEnumValue("Tunnel")
	TUNNEL(CityGMLClass.TUNNEL),
	@XmlEnumValue("TransportationComplex")
	TRANSPORTATION_COMPLEX(CityGMLClass.TRANSPORTATION_COMPLEX),
	@XmlEnumValue("Road")
	ROAD(CityGMLClass.ROAD),
	@XmlEnumValue("Track")
	TRACK(CityGMLClass.TRACK),
	@XmlEnumValue("Square")
	SQUARE(CityGMLClass.SQUARE),
	@XmlEnumValue("Railway")
	RAILWAY(CityGMLClass.RAILWAY),
	@XmlEnumValue("CityFurniture")
	CITY_FURNITURE(CityGMLClass.CITY_FURNITURE),
	@XmlEnumValue("LandUse")
	LAND_USE(CityGMLClass.LAND_USE),
	@XmlEnumValue("WaterBody")
	WATER_BODY(CityGMLClass.WATER_BODY),
	@XmlEnumValue("PlantCover")
	PLANT_COVER(CityGMLClass.PLANT_COVER),
	@XmlEnumValue("SolitaryVegetationObject")
	SOLITARY_VEGETATION_OBJECT(CityGMLClass.SOLITARY_VEGETATION_OBJECT),
	@XmlEnumValue("ReliefFeature")
	RELIEF_FEATURE(CityGMLClass.RELIEF_FEATURE),
	@XmlEnumValue("GenericCityObject")
	GENERIC_CITY_OBJECT(CityGMLClass.GENERIC_CITY_OBJECT),
	@XmlEnumValue("CityObjectGroup")
	CITY_OBJECT_GROUP(CityGMLClass.CITY_OBJECT_GROUP);
	
	private final CityGMLClass cityGMLClass;
	
	private FeatureTypeName(CityGMLClass cityGMLClass) {
		this.cityGMLClass = cityGMLClass;
	}

	public CityGMLClass getCityGMLClass() {
		return cityGMLClass;
	}
	
	public static FeatureTypeName fromCityGMLClass(CityGMLClass cityGMLClass) {
		for (FeatureTypeName typeName : values())
			if (typeName.getCityGMLClass() == cityGMLClass)
				return typeName;
		
		return null;
	}
}