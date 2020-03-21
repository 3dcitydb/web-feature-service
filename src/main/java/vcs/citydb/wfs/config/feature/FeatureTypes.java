package vcs.citydb.wfs.config.feature;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.model.citygml.ade.binding.ADEContext;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.ade.ADEModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@XmlType(name="FeatureTypesType", propOrder={
		"cityGMLFeatureTypes",
		"adeFeatureTypes",
		"versions"
})
public class FeatureTypes {
	@XmlElement(name="featureType", nillable=false, required=true)
	private LinkedHashSet<CityGMLFeatureType> cityGMLFeatureTypes;
	@XmlElement(name="adeFeatureType", nillable=false)
	private LinkedHashSet<ADEFeatureType> adeFeatureTypes;
	@XmlElement(name="version", nillable=false, required=true)
	private LinkedHashSet<CityGMLVersionType> versions;

	@XmlTransient
	private Map<QName, FeatureType> featureTypes;

	public FeatureTypes() {
		cityGMLFeatureTypes = new LinkedHashSet<>();
		adeFeatureTypes = new LinkedHashSet<>();
		versions = new LinkedHashSet<>();
	}

	public Collection<FeatureType> getAdvertisedFeatureTypes() {
		return getFeatureTypes().values();
	}

	public boolean contains(QName featureName) {
		return getFeatureTypes().keySet().contains(featureName);
	}

	public Set<Module> getModules() {
		Set<Module> result = new HashSet<>();
		for (QName name : getFeatureTypes().keySet())
			result.add(Modules.getModule(name.getNamespaceURI()));

		return result;
	}

	public CityGMLVersion getDefaultVersion() {
		for (CityGMLVersionType version : versions)
			if (version.isDefault())
				return version.getValue().getCityGMLVersion();

		return versions.size() == 1 ? 
				versions.iterator().next().getValue().getCityGMLVersion() : CityGMLVersion.DEFAULT;
	}

	public List<CityGMLVersion> getVersions() {
		List<CityGMLVersion> result = new ArrayList<>();
		for (CityGMLVersionType version : versions) {
			if (version.getValue() == null)
				continue;

			result.add(version.getValue().getCityGMLVersion());
		}

		return result;
	}

	public void preprocessFeatureTypes() {
		getFeatureTypes();
	}

	private Map<QName, FeatureType> getFeatureTypes() {
		if (featureTypes != null)
			return featureTypes;

		featureTypes = new LinkedHashMap<>();

		// add CityGML feature types
		for (CityGMLFeatureType type : cityGMLFeatureTypes) {				
			for (CityGMLVersion version : getVersions()) {				
				QName name = type.getQName(version);
				if (name != null)
					featureTypes.put(name, new FeatureType(name, type));
			}
		}

		// check and add ADE feature types
		SchemaMapping schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		Logger log = Logger.getInstance();
		ADEExtensionManager adeManager = ADEExtensionManager.getInstance();

		for (ADEFeatureType type : adeFeatureTypes) {
			QName name = type.getQName();
			if (name == null)
				continue;

			try {
				org.citydb.database.schema.mapping.FeatureType featureType = schemaMapping.getFeatureType(name);
				if (featureType == null)
					throw new ADEException("Failed to find a definition for the feature type in the database schema mapping.");

				if (!featureType.isTopLevel())
					throw new ADEException("The feature type is not top-level.");

				ADEExtension adeExtension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
				if (adeExtension == null || !adeExtension.isEnabled())
					throw new ADEException("The ADE extension of the feature type is disabled.");

				boolean hasLocalSchemaFile = false;
				for (ADEContext adeContext : adeExtension.getADEContexts()) {
					for (ADEModule adeModule : adeContext.getADEModules()) {
						if (adeModule.getNamespaceURI().equals(name.getNamespaceURI())) {
							URL schemaLocation = adeModule.getSchemaResource();
							if (schemaLocation != null) {
								String protocol = schemaLocation.getProtocol();
								if ("jar".equalsIgnoreCase(protocol)) {
									hasLocalSchemaFile = true;
									break;
								} else if ("file".equalsIgnoreCase(protocol)
										&& Files.exists(Paths.get(schemaLocation.getFile()))) {
									hasLocalSchemaFile = true;
									break;
								}
							}
						}

						if (hasLocalSchemaFile)
							break;
					}
				}

				if (!hasLocalSchemaFile)
					throw new ADEException("No local ADE XML schema file provided for this feature type.");

				boolean isAvailable = false;
				for (CityGMLVersion version : getVersions()) {
					if (featureType.isAvailableForCityGML(version)) {
						isAvailable = true;
						break;
					}
				}

				if (!isAvailable)
					throw new ADEException("The feature type is not available for the CityGML version(s): " 
							+ getVersions().stream().map(v -> v.toString()).collect(Collectors.joining(",")));

				featureTypes.put(name, new FeatureType(name, type));
			} catch (ADEException e) {
				log.error(new StringBuilder("The ADE feature type '").append(name).append("' will not be advertised.").toString());
				log.error(new StringBuilder("Cause: ").append(e.getMessage()).toString());
			}
		}

		cityGMLFeatureTypes = null;
		adeFeatureTypes = null;

		return featureTypes;
	}

}
