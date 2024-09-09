package vcs.citydb.wfs.kvp;

import net.opengis.wfs._2.BaseRequestType;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.StringParser;

import java.util.Map;

public class BaseRequestReader {

    public void read(BaseRequestType wfsRequest, Map<String, String> parameters) throws WFSException {
        try {
            if (parameters.containsKey(KVPConstants.SERVICE))
                wfsRequest.setService(new StringParser().parse(KVPConstants.SERVICE, parameters.get(KVPConstants.SERVICE)));
            else
                throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The request lacks the mandatory " + KVPConstants.SERVICE + " parameter.", KVPConstants.SERVICE);

            if (parameters.containsKey(KVPConstants.VERSION))
                wfsRequest.setVersion(new StringParser().parse(KVPConstants.VERSION, parameters.get(KVPConstants.VERSION)));
            else
                throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The request lacks the mandatory " + KVPConstants.VERSION + " parameter.", KVPConstants.VERSION);

        } catch (KVPParseException e) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getParameter(), e.getCause());
        }
    }

}
