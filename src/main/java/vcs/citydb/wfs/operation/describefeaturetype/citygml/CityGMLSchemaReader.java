package vcs.citydb.wfs.operation.describefeaturetype.citygml;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.Namespace;
import org.citygml4j.model.citygml.ade.binding.ADEContext;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.ade.ADEModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.SchemaReaderException;
import vcs.citydb.wfs.operation.describefeaturetype.SchemaReader;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class CityGMLSchemaReader implements SchemaReader {
	private Set<FeatureType> featureTypes;
	private CityGMLVersion version;
	private ServletContext servletContext;

	@Override
	public String getMimeType() {
		return "text/xml";
	}

	@Override
	public void initializeContext(Set<FeatureType> featureTypes, CityGMLVersion version, ServletContext servletContext) throws SchemaReaderException {
		this.featureTypes = featureTypes;
		this.version = version;
		this.servletContext = servletContext;
	}

	@Override
	public InputStream openSchema() throws SchemaReaderException, IOException {
		String localSchemaPath = version == CityGMLVersion.v2_0_0 ? Constants.CITYGML_2_0_SCHEMAS_PATH : Constants.CITYGML_1_0_SCHEMAS_PATH;	
		Set<Module> modules = null;
		
		if (!featureTypes.isEmpty()) {
			modules = new HashSet<>();
			for (FeatureType featureType : featureTypes) {
				Namespace namespace = featureType.getSchema().getNamespace(version);			
				Module module = Modules.getModule(namespace.getURI());
				if (module == null)
					throw new SchemaReaderException("The feature type '" + new QName(namespace.getURI(), featureType.getPath()) + "' is not part of CityGML.");

				modules.add(module);
			}
		}
		
		if (modules != null && modules.size() == 1) {
			Module module = modules.iterator().next();
			if (module instanceof CityGMLModule) {
				String fileName = module.getSchemaLocation();
				fileName = fileName.substring(fileName.lastIndexOf("/"), fileName.length());				
				return servletContext.getResourceAsStream(localSchemaPath + fileName);
			} else if (module instanceof ADEModule)
				return ((ADEModule)module).getSchemaResource().openStream();
		}

		return new ByteArrayInputStream(createDynamicXMLSchema(version).getBytes(StandardCharsets.UTF_8));
	}
	
	private String createDynamicXMLSchema(CityGMLVersion version) {
		StringBuilder schema = new StringBuilder()
				.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
				.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\n")
				.append("  <xs:annotation>\n")
				.append("    <xs:documentation>\n")
				.append("      This XML Schema file has been dynamically created by the ").append(Constants.DEFAULT_OWS_TITLE).append(".\n")
				.append("      It combines the official OGC CityGML ").append(version.toString()).append(" schemas and advertised ADE schemas.\n")
				.append("    </xs:documentation>\n")
				.append("  </xs:annotation>\n");

		for (CityGMLModule module : version.getCityGMLModules()) {
			schema.append("  <xs:import namespace=\"").append(module.getNamespaceURI())
			.append("\" schemaLocation=\"").append(module.getSchemaLocation()).append("\"/>\n");
		}

		for (ADEExtension adeExtension : ADEExtensionManager.getInstance().getEnabledExtensions()) {
			for (ADEContext adeContext : adeExtension.getADEContexts()) {
				for (ADEModule module : adeContext.getADEModules()) {
					if (module.getSchemaLocation() != null) 
						schema.append("  <xs:import namespace=\"").append(module.getNamespaceURI())
						.append("\" schemaLocation=\"").append(module.getSchemaLocation()).append("\"/>\n");
				}
			}
		}

		return schema.append("</xs:schema>").toString();
	}

}
