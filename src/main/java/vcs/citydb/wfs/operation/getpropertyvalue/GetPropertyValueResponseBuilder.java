package vcs.citydb.wfs.operation.getpropertyvalue;

import net.opengis.wfs._2.GetPropertyValueType;
import net.opengis.wfs._2.ResultTypeType;
import org.citydb.ade.model.module.CityDBADE100Module;
import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.config.Config;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.util.log.Logger;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.ade.ADEModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.internal.xml.TransformerChainFactory;
import org.citygml4j.util.xml.SAXWriter;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.util.GeometryStripper;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class GetPropertyValueResponseBuilder {
	private final Logger log = Logger.getInstance();
	private final String PRETTY_PRINT = "prettyPrint";
	
	private String valueReference;
	private NamespaceFilter namespaceFilter;
	private CityGMLVersion version;
	private GeometryStripper geometryStripper;
	private Config config;
	private Object eventChannel;
	
	private SAXWriter saxWriter;
	private TransformerChainFactory transformerChainFactory;
	private boolean useSequentialWriting;
	
	public String getMimeType() {
		return "text/xml";
	}

	public void initializeContext(GetPropertyValueType wfsRequest,
			QueryExpression queryExpression,
			Map<String, String> formatOptions,
			NamespaceFilter namespaceFilter,
			Object eventChannel,
			WFSConfig wfsConfig,
			Config config) throws FeatureWriteException {
		this.namespaceFilter = namespaceFilter;
		this.eventChannel = eventChannel;
		this.config = config;
		
		valueReference = queryExpression.getValueReference();
		version = queryExpression.getTargetVersion();
		geometryStripper = wfsConfig.getConstraints().isStripGeometry() ? new GeometryStripper() : null;

		// require sequential writing if a sorting clause is defined for the query
		useSequentialWriting = queryExpression.isSetSorting();

		saxWriter = new SAXWriter();
		saxWriter.setWriteEncoding(true);

		if ("true".equals(formatOptions.get(PRETTY_PRINT)))
			saxWriter.setIndentString(" ");

		// set WFS prefix and schema location
		saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
		saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);

		// set CityGML prefixes and schema locations
		if (wfsRequest.getResultType() == ResultTypeType.RESULTS) {
			// add default prefixes and schema locations
			ModuleContext moduleContext = new ModuleContext(version);

			Module core = moduleContext.getModule(CityGMLModuleType.CORE);
			Module generics = moduleContext.getModule(CityGMLModuleType.GENERICS);
			saxWriter.setPrefix(core.getNamespacePrefix(), core.getNamespaceURI());
			saxWriter.setPrefix(generics.getNamespacePrefix(), generics.getNamespaceURI());
			saxWriter.setSchemaLocation(generics.getNamespaceURI(), generics.getSchemaLocation());
			if (wfsConfig.getConstraints().isExportAppearance()) {
				Module appearance = moduleContext.getModule(CityGMLModuleType.APPEARANCE);
				saxWriter.setPrefix(appearance.getNamespacePrefix(), appearance.getNamespaceURI());
				saxWriter.setSchemaLocation(appearance.getNamespaceURI(), appearance.getSchemaLocation());
			}

			// add XML prefixes and schema locations for non-CityGML modules
			for (Module module : moduleContext.getModules()) {
				if (!(module instanceof CityGMLModule)) {
					// skip 3DCityDB ADE prefix and namespace if metadata shall not be exported
					if ((module == CityDBADE200Module.v3_0 || module == CityDBADE100Module.v3_0)
							&& !wfsConfig.getConstraints().isExportCityDBMetadata())
						continue;

					saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
					if (module instanceof ADEModule)
						saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
				}
			}

			// set XML prefixes and schema locations for selected feature types
			for (FeatureType featureType : queryExpression.getFeatureTypeFilter().getFeatureTypes()) {
				CityGMLModule module = Modules.getCityGMLModule(featureType.getSchema().getNamespace(version).getURI());
				if (module != null) {
					saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
					saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
				}
			}

			// build XSLT transformer chain
			if (wfsConfig.getPostProcessing().getXSLTransformation().isEnabled()
					&& wfsConfig.getPostProcessing().getXSLTransformation().isSetStylesheets()) {
				try {
					List<String> stylesheets = wfsConfig.getPostProcessing().getXSLTransformation().getStylesheets();
					SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
					Templates[] templates = new Templates[stylesheets.size()];

					for (int i = 0; i < stylesheets.size(); i++) {
						Templates template = factory.newTemplates(new StreamSource(new File(stylesheets.get(i))));
						templates[i] = template;
					}

					transformerChainFactory = new TransformerChainFactory(templates);
				} catch (TransformerConfigurationException e) {
					log.error("Failed to compile XSLT stylesheets.");
					log.error("Cause: " + e.getMessage());
					throw new FeatureWriteException("Failed to configure the XSL transformation.");
				}
			}
		}
	}

	public PropertyValueWriter buildPropertyValueWriter(Writer writer) throws FeatureWriteException {
		try {
			saxWriter.setOutput(writer);
			return new PropertyValueWriter(valueReference,
					saxWriter,
					version,
					transformerChainFactory,
					namespaceFilter,
					geometryStripper,
					useSequentialWriting,
					eventChannel,
					config);
		} catch (DatatypeConfigurationException e) {
			throw new FeatureWriteException("Failed to create CityGML response writer.", e);
		}
	}

}
