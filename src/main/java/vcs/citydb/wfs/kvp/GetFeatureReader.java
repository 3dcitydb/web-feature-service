package vcs.citydb.wfs.kvp;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.GetFeatureType;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.StringParser;

import javax.xml.bind.JAXBElement;
import javax.xml.validation.Schema;
import java.util.List;
import java.util.Map;

public class GetFeatureReader extends KVPRequestReader {
	private final BaseRequestReader baseRequestReader;
	private final QueryExpressionReader queryExpressionReader;
	
	public GetFeatureReader(Map<String, String> parameters, Schema wfsSchema, CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) {
		super(parameters, wfsConfig);
		
		baseRequestReader = new BaseRequestReader();
		queryExpressionReader = new QueryExpressionReader(new net.opengis.wfs._2.ObjectFactory());
	}

	@Override
	public GetFeatureType readRequest() throws WFSException {
		GetFeatureType wfsRequest = new GetFeatureType();
		baseRequestReader.read(wfsRequest, parameters);

		try {			
			// standard presentation parameters
			if (parameters.containsKey(KVPConstants.OUTPUT_FORMAT))
				wfsRequest.setOutputFormat(new StringParser().parse(KVPConstants.OUTPUT_FORMAT, parameters.get(KVPConstants.OUTPUT_FORMAT)));

		} catch (KVPParseException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getCause());
		}
		
		// queries
		List<JAXBElement<? extends AbstractQueryExpressionType>> queries = queryExpressionReader.read(parameters, getNamespaces(), true);
		wfsRequest.getAbstractQueryExpression().addAll(queries);
		
		return wfsRequest;
	}

}
