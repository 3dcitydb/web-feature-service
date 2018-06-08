package vcs.citydb.wfs.kvp;

import java.util.Map;

import net.opengis.wfs._2.BaseRequestType;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.StringParser;

public class BaseRequestReader {

	public void read(BaseRequestType wfsRequest, Map<String, String> parameters) throws WFSException {
		try {
			if (parameters.containsKey(KVPConstants.SERVICE))
				wfsRequest.setService(new StringParser().parse(KVPConstants.SERVICE, parameters.get(KVPConstants.SERVICE)));
			
			if (parameters.containsKey(KVPConstants.VERSION))
				wfsRequest.setVersion(new StringParser().parse(KVPConstants.VERSION, parameters.get(KVPConstants.VERSION)));
			
		} catch (KVPParseException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getCause());
		}
	}

}
