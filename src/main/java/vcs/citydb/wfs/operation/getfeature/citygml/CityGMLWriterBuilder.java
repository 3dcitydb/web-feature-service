package vcs.citydb.wfs.operation.getfeature.citygml;

import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ResultTypeType;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.config.Config;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.ade.ADEModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXWriter;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.operation.getfeature.FeatureWriter;
import vcs.citydb.wfs.operation.getfeature.GetFeatureResponseBuilder;
import vcs.citydb.wfs.operation.getfeature.QueryExpression;
import vcs.citydb.wfs.util.GeometryStripper;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class CityGMLWriterBuilder implements GetFeatureResponseBuilder {
	private final String PRETTY_PRINT = "prettyPrint";

	private CityGMLVersion version;
	private GeometryStripper geometryStripper;
	private UIDCacheManager uidCacheManager;
	private Config exporterConfig;
	private Object eventChannel;

	private SAXWriter saxWriter;

	@Override
	public String getMimeType() {
		return "text/xml";
	}

	@Override
	public boolean supportsHitsResponse() {
		return true;
	}

	@Override
	public void initializeContext(GetFeatureType wfsRequest, 
			List<QueryExpression> queryExpressions,
			Map<String, String> formatOptions,
			GeometryStripper geometryStripper,
			UIDCacheManager uidCacheManager,
			Object eventChannel,
			WFSConfig wfsConfig,
			Config exporterConfig) throws FeatureWriteException {
		this.geometryStripper = geometryStripper;
		this.uidCacheManager = uidCacheManager;
		this.eventChannel = eventChannel;
		this.exporterConfig = exporterConfig;		
		version = queryExpressions.get(0).getTargetVersion();

		saxWriter = new SAXWriter();
		saxWriter.setWriteEncoding(true);

		if ("true".equals(formatOptions.get(PRETTY_PRINT)))
			saxWriter.setIndentString(" ");

		// set WFS prefix and schema location in case we do not have to return the bare feature
		if (queryExpressions.size() > 1 || !queryExpressions.get(0).isGetFeatureById()) {
			saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);
		}

		// set CityGML prefixes and schema locations if we have to return feature instances
		if (wfsRequest.getResultType() == ResultTypeType.RESULTS) {
			// add default prefixes and schema locations
			ModuleContext moduleContext = new ModuleContext(version);

			Module core = moduleContext.getModule(CityGMLModuleType.CORE);
			Module generics = moduleContext.getModule(CityGMLModuleType.GENERICS);
			saxWriter.setPrefix(core.getNamespacePrefix(), core.getNamespaceURI());
			saxWriter.setPrefix(generics.getNamespacePrefix(), generics.getNamespaceURI());
			saxWriter.setSchemaLocation(generics.getNamespaceURI(), generics.getSchemaLocation());

			// add XML prefixes and schema locations for non-CityGML modules
			for (Module module : moduleContext.getModules()) {
				if (!(module instanceof CityGMLModule)) {
					saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
					if (module instanceof ADEModule)
						saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
				}
			}

			// set XML prefixes and schema locations for selected feature types
			for (QueryExpression queryExpression : queryExpressions) {
				for (FeatureType featureType : queryExpression.getFeatureTypeFilter().getFeatureTypes()) {
					if (featureType.isAvailableForCityGML(version)) {
						CityGMLModule module = Modules.getCityGMLModule(featureType.getSchema().getNamespace(version).getURI());
						if (module != null) {
							saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
							saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
						}
					}
				}
			}

			// add CityDB ADE namespace and schema location if required
			if (wfsConfig.getOperations().getGetFeature().isUseCityDBADE()) {
				saxWriter.setPrefix(MappingConstants.CITYDB_ADE_NAMESPACE_PREFIX, MappingConstants.CITYDB_ADE_NAMESPACE_URI);
				saxWriter.setSchemaLocation(MappingConstants.CITYDB_ADE_NAMESPACE_URI, MappingConstants.CITYDB_ADE_SCHEMA_LOCATIONS.get(version));
			}
		}
	}

	@Override
	public FeatureWriter buildFeatureWriter(OutputStream stream, String encoding) throws FeatureWriteException {
		try {
			saxWriter.setOutput(stream, encoding);
			return new CityGMLWriter(saxWriter, version, geometryStripper, uidCacheManager, eventChannel, exporterConfig);			
		} catch (IOException | DatatypeConfigurationException e) {
			throw new FeatureWriteException("Failed to create CityGML response writer.", e);
		}
	}

}
