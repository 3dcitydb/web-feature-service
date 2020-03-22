package vcs.citydb.wfs.operation.getfeature.cityjson;

import net.opengis.wfs._2.GetFeatureType;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.log.Logger;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.cityjson.CityJSONBuilder;
import org.citygml4j.builder.cityjson.CityJSONBuilderException;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONOutputFactory;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONWriteException;
import org.citygml4j.builder.cityjson.marshal.util.DefaultTextureVerticesBuilder;
import org.citygml4j.builder.cityjson.marshal.util.DefaultVerticesBuilder;
import org.citygml4j.builder.cityjson.marshal.util.DefaultVerticesTransformer;
import org.citygml4j.cityjson.metadata.MetadataType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.operation.getfeature.FeatureWriter;
import vcs.citydb.wfs.operation.getfeature.GetFeatureResponseBuilder;
import vcs.citydb.wfs.operation.getfeature.QueryExpression;
import vcs.citydb.wfs.util.GeometryStripper;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class CityJSONWriterBuilder implements GetFeatureResponseBuilder {
	private final Logger log = Logger.getInstance();
	private final String PRETTY_PRINT = "prettyPrint";
	private final String SIGNIFICANT_DIGITS = "significantDigits";
	private final String SIGNIFICANT_TEXTURE_DIGITS = "significantTextureDigits";
	private final String TRANSFORM_VERTICES = "transformVertices";
	private final String GENERATE_CITYGML_METADATA = "generateCityGMLMetadata";
	
	private CityJSONOutputFactory factory;
	private Map<String, String> formatOptions;
	private GeometryStripper geometryStripper;
	private UIDCacheManager uidCacheManager;
	private Object eventChannel;
	private Config config;

	private MetadataType metadata;

	@Override
	public String getMimeType() {
		return "application/json";
	}
	
	@Override
	public boolean supportsHitsResponse() {
		return false;
	}

	@Override
	public void initializeContext(GetFeatureType wfsRequest, 
			List<QueryExpression> queryExpressions,
			Map<String, String> formatOptions, 
			GeometryStripper geometryStripper, 
			UIDCacheManager uidCacheManager,
			Object eventChannel, 
			WFSConfig wfsConfig, 
			Config config) throws FeatureWriteException {
		this.formatOptions = formatOptions;
		this.geometryStripper = geometryStripper;
		this.uidCacheManager = uidCacheManager;
		this.eventChannel = eventChannel;
		this.config = config;

		try {
			CityJSONBuilder builder = CityGMLContext.getInstance().createCityJSONBuilder();
			factory = builder.createCityJSONOutputFactory();
			
			if (formatOptions.containsKey(SIGNIFICANT_DIGITS)) {
				try {
					Integer significantDigits = Integer.parseInt(formatOptions.get(SIGNIFICANT_DIGITS));
					factory.setVerticesBuilder(new DefaultVerticesBuilder().withSignificantDigits(significantDigits));
				} catch (NumberFormatException e) {
					log.warn("The '" + SIGNIFICANT_DIGITS + "' format options requires an integer value.");
				}
			}
			
			if (formatOptions.containsKey(SIGNIFICANT_TEXTURE_DIGITS)) {
				try {
					int significantDigits = Integer.parseInt(formatOptions.get(SIGNIFICANT_TEXTURE_DIGITS));
					factory.setTextureVerticesBuilder(new DefaultTextureVerticesBuilder().withSignificantDigits(significantDigits));
				} catch (NumberFormatException e) {
					log.warn("The '" + SIGNIFICANT_TEXTURE_DIGITS + "' format options requires an integer value.");
				}
			}
			
			if ("true".equals(formatOptions.get(TRANSFORM_VERTICES)))
				factory.setVerticesTransformer(new DefaultVerticesTransformer());

			if ("false".equals(formatOptions.get(GENERATE_CITYGML_METADATA)))
				factory.setGenerateCityGMLMetadata(false);
			else
				factory.setGenerateCityGMLMetadata(true);
				
		} catch (CityJSONBuilderException e) {
			throw new FeatureWriteException("Failed to initialize CityJSON response builder.", e);
		}
		
		metadata = new MetadataType();
		DatabaseSrs targetSRS = null;
		
		for (QueryExpression queryExpression : queryExpressions) {
			if (targetSRS == null)
				targetSRS = queryExpression.getTargetSrs();
			else if (targetSRS.getSrid() != queryExpression.getTargetSrs().getSrid())
				throw new FeatureWriteException("Multiple target coordinate reference systems are not supported by CityJSON.");
		}

		metadata.setReferenceSystem(targetSRS.getSrid());
	}

	@Override
	public FeatureWriter buildFeatureWriter(OutputStream stream, String encoding) throws FeatureWriteException {
		try {
			CityJSONChunkWriter writer = factory.createCityJSONChunkWriter(stream, encoding);
			writer.setMetadata(metadata);

			if ("true".equals(formatOptions.get(PRETTY_PRINT)))
				writer.setIndent(" ");

			return new CityJSONWriter(writer, geometryStripper, uidCacheManager, eventChannel, config);
		} catch (CityJSONWriteException e) {
			throw new FeatureWriteException("Failed to create CityJSON response writer.", e);
		}
	}

}
