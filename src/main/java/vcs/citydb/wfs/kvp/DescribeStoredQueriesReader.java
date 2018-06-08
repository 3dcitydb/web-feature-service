package vcs.citydb.wfs.kvp;

import java.util.Map;

import net.opengis.wfs._2.DescribeStoredQueriesType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.FlatValueListParser;
import vcs.citydb.wfs.kvp.parser.StringParser;

public class DescribeStoredQueriesReader extends KVPRequestReader {
	private final BaseRequestReader baseRequestReader;

	public DescribeStoredQueriesReader(Map<String, String> parameters, WFSConfig wfsConfig) {
		super(parameters, wfsConfig);

		baseRequestReader = new BaseRequestReader();
	}

	@Override
	public DescribeStoredQueriesType readRequest() throws WFSException {
		DescribeStoredQueriesType wfsRequest = new DescribeStoredQueriesType();
		baseRequestReader.read(wfsRequest, parameters);

		try {
			if (parameters.containsKey(KVPConstants.STOREDQUERY_ID))
				wfsRequest.setStoredQueryId(new FlatValueListParser<String>(new StringParser()).parse(KVPConstants.STOREDQUERY_ID, parameters.get(KVPConstants.STOREDQUERY_ID)));

		} catch (KVPParseException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getCause());
		}

		return wfsRequest;
	}

}