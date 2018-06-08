package vcs.citydb.wfs.config.feature;

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlType(name="FeatureTypeType", propOrder={
		"name",
		"titles",
		"abstracts",
		"keywords",
		"wgs84BoundingBoxes",
		"metadataURLs",
		"extendedDescription"
})
public class CityGMLFeatureType extends FeatureType {
	@XmlElement(required=true)
	private CityGMLFeatureTypeName name;
	
	protected QName getQName(CityGMLVersion version) {
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
		return Objects.hash(name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CityGMLFeatureType && name == ((CityGMLFeatureType)obj).name)
			return true;
		
		return super.equals(obj);
	}

}
