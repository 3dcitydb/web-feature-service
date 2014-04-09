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