package vcs.citydb.wfs.operation.describefeaturetype;

import net.opengis.wfs._2.DescribeFeatureTypeType;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.util.log.Logger;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.DescribeFeatureTypeOutputFormat;
import vcs.citydb.wfs.config.operation.OutputFormat;
import vcs.citydb.wfs.exception.SchemaReaderException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.operation.describefeaturetype.citygml.CityGMLSchemaReader;
import vcs.citydb.wfs.operation.describefeaturetype.cityjson.CityJSONSchemaReader;
import vcs.citydb.wfs.operation.filter.FeatureTypeHandler;
import vcs.citydb.wfs.util.LoggerUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class DescribeFeatureTypeHandler {
	private final Logger log = Logger.getInstance();
	private final WFSConfig wfsConfig;

	private final BaseRequestHandler baseRequestHandler;
	private final FeatureTypeHandler featureTypeHandler;

	public DescribeFeatureTypeHandler(WFSConfig wfsConfig) {
		this.wfsConfig = wfsConfig;

		baseRequestHandler = new BaseRequestHandler(wfsConfig);
		featureTypeHandler = new FeatureTypeHandler();
	}

	public void doOperation(DescribeFeatureTypeType wfsRequest,
			ServletContext servletContext,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		log.info(LoggerUtil.getLogMessage(request, "Accepting DescribeFeatureType request."));
		final String operationHandle = wfsRequest.getHandle();

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// check output format
		if (wfsRequest.isSetOutputFormat() && !wfsConfig.getOperations().getDescribeFeatureType().supportsOutputFormat(wfsRequest.getOutputFormat())) {
			WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE);
			message.addExceptionText("The output format of a DescribeFeatureType request must match one of the following formats:");
			message.addExceptionTexts(wfsConfig.getOperations().getDescribeFeatureType().getOutputFormatsAsString());
			message.setLocator(KVPConstants.OUTPUT_FORMAT);

			throw new WFSException(message);
		}

		Set<FeatureType> featureTypes = featureTypeHandler.getFeatureTypes(wfsRequest.getTypeName(), true, KVPConstants.TYPE_NAME, operationHandle);
		CityGMLVersion version = featureTypeHandler.getCityGMLVersion();
		if (version == null)
			version = wfsConfig.getFeatureTypes().getDefaultVersion();

		SchemaReader schemaReader;
		try {
			schemaReader = getSchemaReader(wfsRequest, featureTypes, version, servletContext);
		} catch (SchemaReaderException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to initialize the schema reader.", operationHandle, e);
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(schemaReader.openSchema()))) {
			response.setContentType(schemaReader.getMimeType());
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			PrintWriter writer = response.getWriter();

			String line;
			while ((line = reader.readLine()) != null)
				writer.println(line);

		} catch (IOException | SchemaReaderException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to process schema document.", operationHandle, e);
		}

		log.info(LoggerUtil.getLogMessage(request, "DescribeFeatureType operation successfully finished."));
	}

	private SchemaReader getSchemaReader(DescribeFeatureTypeType wfsRequest, Set<FeatureType> featureTypes, CityGMLVersion version, ServletContext servletContext) throws SchemaReaderException {
		OutputFormat outputFormat = wfsConfig.getOperations().getDescribeFeatureType().getOutputFormat(wfsRequest.isSetOutputFormat() ? 
				wfsRequest.getOutputFormat() : DescribeFeatureTypeOutputFormat.GML3_1.value());

		SchemaReader schemaReader;
		switch (outputFormat.getName()) {
			case "application/gml+xml; version=3.1":
				schemaReader = new CityGMLSchemaReader();
				break;
			case "application/json":
				schemaReader = new CityJSONSchemaReader();
				break;
			default:
				throw new SchemaReaderException("No schema reader has been registered for the output format '" + outputFormat.getName() + "'.");
		}
		
		schemaReader.initializeContext(featureTypes, version, servletContext);
		
		return schemaReader;
	}
}
