package vcs.citydb.wfs.kvp;

import net.opengis.wfs._2.DropStoredQueryType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.StringParser;

import java.util.Map;

public class DropStoredQueryReader extends KVPRequestReader {
    private final BaseRequestReader baseRequestReader;

    public DropStoredQueryReader(Map<String, String> parameters, WFSConfig wfsConfig) {
        super(parameters, wfsConfig);
        baseRequestReader = new BaseRequestReader();
    }

    @Override
    public DropStoredQueryType readRequest() throws WFSException {
        DropStoredQueryType wfsRequest = new DropStoredQueryType();
        baseRequestReader.read(wfsRequest, parameters);

        try {
            if (parameters.containsKey(KVPConstants.STOREDQUERY_ID))
                wfsRequest.setId(new StringParser().parse(KVPConstants.STOREDQUERY_ID, parameters.get(KVPConstants.STOREDQUERY_ID)));

        } catch (KVPParseException e) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getParameter(), e.getCause());
        }

        return wfsRequest;
    }

    @Override
    public String getOperationName() {
        return KVPConstants.DROP_STORED_QUERY;
    }
}