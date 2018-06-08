package vcs.citydb.wfs.operation;

import org.citydb.util.Util;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import net.opengis.wfs._2.BaseRequestType;

public class BaseRequestHandler {
	private final WFSConfig wfsConfig;
	
	public BaseRequestHandler(WFSConfig wfsConfig) {
		this.wfsConfig = wfsConfig;
	}

	public void validate(BaseRequestType wfsRequest) throws WFSException {
		final String operationHandle = wfsRequest.getHandle();
		
		// check service attribute
		if (!wfsRequest.isSetService() || !Constants.WFS_SERVICE_STRING.equals(wfsRequest.getService()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'service' must match the fixed value '" + Constants.WFS_SERVICE_STRING + "'.", operationHandle);

		// check version attribute
		if (!wfsRequest.isSetVersion() || !wfsConfig.getCapabilities().getSupportedWFSVersions().contains(wfsRequest.getVersion()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'version' must match one of the supported version numbers '" + Util.collection2string(wfsConfig.getCapabilities().getSupportedWFSVersions(), ", ") + "'.", operationHandle);
	}
	
}
