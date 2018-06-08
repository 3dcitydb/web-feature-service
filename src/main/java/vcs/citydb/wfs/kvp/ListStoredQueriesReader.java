package vcs.citydb.wfs.kvp;

import java.util.Map;

import net.opengis.wfs._2.ListStoredQueriesType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;

public class ListStoredQueriesReader extends KVPRequestReader {
	private final BaseRequestReader baseRequestReader;
	
	public ListStoredQueriesReader(Map<String, String> parameters, WFSConfig wfsConfig) {
		super(parameters, wfsConfig);
		
		baseRequestReader = new BaseRequestReader();
	}
	
	@Override
	public ListStoredQueriesType readRequest() throws WFSException {
		ListStoredQueriesType wfsRequest = new ListStoredQueriesType();
		baseRequestReader.read(wfsRequest, parameters);

		return wfsRequest;
	}

}