package vcs.citydb.wfs.kvp;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.GetPropertyValueType;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.BigIntegerParser;
import vcs.citydb.wfs.kvp.parser.ResolveValueParser;
import vcs.citydb.wfs.kvp.parser.ResultTypeParser;
import vcs.citydb.wfs.kvp.parser.StringParser;

import javax.xml.bind.JAXBElement;
import javax.xml.validation.Schema;
import java.util.List;
import java.util.Map;

public class GetPropertyValueReader extends KVPRequestReader {
	private final BaseRequestReader baseRequestReader;
	private final QueryExpressionReader queryExpressionReader;
	
	public GetPropertyValueReader(Map<String, String> parameters, Schema wfsSchema, CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) {
		super(parameters, wfsConfig);
		baseRequestReader = new BaseRequestReader();
		queryExpressionReader = new QueryExpressionReader(
				new net.opengis.wfs._2.ObjectFactory(), 
				new net.opengis.fes._2.ObjectFactory(), 
				new net.opengis.gml.ObjectFactory(), 
				wfsSchema,
				cityGMLBuilder, 
				wfsConfig);
	}

	@Override
	public GetPropertyValueType readRequest() throws WFSException {
		GetPropertyValueType wfsRequest = new GetPropertyValueType();
		baseRequestReader.read(wfsRequest, parameters);

		try {			
			// standard presentation parameters
			if (parameters.containsKey(KVPConstants.START_INDEX))
				wfsRequest.setStartIndex(new BigIntegerParser().parse(KVPConstants.START_INDEX, parameters.get(KVPConstants.START_INDEX)));

			if (parameters.containsKey(KVPConstants.COUNT))
				wfsRequest.setCount(new BigIntegerParser().parse(KVPConstants.COUNT, parameters.get(KVPConstants.COUNT)));

			if (parameters.containsKey(KVPConstants.OUTPUT_FORMAT))
				wfsRequest.setOutputFormat(new StringParser().parse(KVPConstants.OUTPUT_FORMAT, parameters.get(KVPConstants.OUTPUT_FORMAT)));

			if (parameters.containsKey(KVPConstants.RESULT_TYPE))
				wfsRequest.setResultType(new ResultTypeParser().parse(KVPConstants.RESULT_TYPE, parameters.get(KVPConstants.RESULT_TYPE)));

			// standard resolve parameters
			if (parameters.containsKey(KVPConstants.RESOLVE))
				wfsRequest.setResolve(new ResolveValueParser().parse(KVPConstants.RESOLVE, parameters.get(KVPConstants.RESOLVE)));

			if (parameters.containsKey(KVPConstants.RESOLVE_DEPTH))
				wfsRequest.setResolveDepth(new StringParser().parse(KVPConstants.RESOLVE_DEPTH, parameters.get(KVPConstants.RESOLVE_DEPTH)));

			if (parameters.containsKey(KVPConstants.RESOLVE_TIMEOUT))
				wfsRequest.setResolveTimeout(new BigIntegerParser().parse(KVPConstants.RESOLVE_TIMEOUT, parameters.get(KVPConstants.RESOLVE_TIMEOUT)));
			
			// request-specific parameters
			if (parameters.containsKey(KVPConstants.VALUE_REFERENCE))
				wfsRequest.setValueReference(new StringParser().parse(KVPConstants.VALUE_REFERENCE, parameters.get(KVPConstants.VALUE_REFERENCE)));

			if (parameters.containsKey(KVPConstants.RESOLVE_PATH))
				wfsRequest.setResolvePath(new StringParser().parse(KVPConstants.RESOLVE_PATH, parameters.get(KVPConstants.RESOLVE_PATH)));

		} catch (KVPParseException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getParameter(), e.getCause());
		}
		
		// queries
		List<JAXBElement<? extends AbstractQueryExpressionType>> queries = queryExpressionReader.read(parameters, KVPConstants.GET_PROPERTY_VALUE, getNamespaces(), false);
		wfsRequest.setAbstractQueryExpression(queries.get(0));
		
		return wfsRequest;
	}

	@Override
	public String getOperationName() {
		return KVPConstants.GET_PROPERTY_VALUE;
	}
}
