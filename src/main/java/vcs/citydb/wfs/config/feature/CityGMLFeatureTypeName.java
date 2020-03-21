package vcs.citydb.wfs.config.feature;

import org.citygml4j.model.citygml.CityGMLClass;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FeatureTypeNameType")
@XmlEnum
public enum CityGMLFeatureTypeName {
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
	
	private CityGMLFeatureTypeName(CityGMLClass cityGMLClass) {
		this.cityGMLClass = cityGMLClass;
	}

	public CityGMLClass getCityGMLClass() {
		return cityGMLClass;
	}
	
	public static CityGMLFeatureTypeName fromCityGMLClass(CityGMLClass cityGMLClass) {
		for (CityGMLFeatureTypeName typeName : values())
			if (typeName.getCityGMLClass() == cityGMLClass)
				return typeName;
		
		return null;
	}
}