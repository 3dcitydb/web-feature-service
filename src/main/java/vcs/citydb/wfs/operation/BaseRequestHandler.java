package vcs.citydb.wfs.operation;

import net.opengis.wfs._2.BaseRequestType;
import org.citydb.core.util.Util;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.KVPConstants;

public class BaseRequestHandler {
	private final WFSConfig wfsConfig;
	
	public BaseRequestHandler(WFSConfig wfsConfig) {
		this.wfsConfig = wfsConfig;
	}

	public void validate(BaseRequestType wfsRequest) throws WFSException {
		// check service attribute
		if (!wfsRequest.isSetService())
			throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The request lacks the mandatory " + KVPConstants.SERVICE + " parameter.", KVPConstants.SERVICE);
		else if (!Constants.WFS_SERVICE_STRING.equals(wfsRequest.getService()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'service' must match the fixed value '" + Constants.WFS_SERVICE_STRING + "'.", KVPConstants.SERVICE);

		// check version attribute
		if (!wfsRequest.isSetVersion())
			throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The request lacks the mandatory " + KVPConstants.VERSION + " parameter.", KVPConstants.VERSION);
		else if (!wfsConfig.getCapabilities().getSupportedWFSVersions().contains(wfsRequest.getVersion()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'version' must match one of the supported version numbers '" + Util.collection2string(wfsConfig.getCapabilities().getSupportedWFSVersions(), ", ") + "'.", KVPConstants.VERSION);
	}
	
}
