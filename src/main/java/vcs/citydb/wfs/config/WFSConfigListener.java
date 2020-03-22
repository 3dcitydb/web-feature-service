package vcs.citydb.wfs.config;

import net.opengis.ows._1.CodeType;
import vcs.citydb.wfs.config.capabilities.OWSMetadata;

import javax.xml.bind.Unmarshaller.Listener;
import java.util.Collections;
import java.util.List;

public class WFSConfigListener extends Listener {

	@Override
	public void afterUnmarshal(Object target, Object parent) {
		if (target instanceof OWSMetadata) {
			OWSMetadata owsMetadata = (OWSMetadata)target;
			
			// set service type
			CodeType serviceType = new CodeType();
			serviceType.setValue(Constants.WFS_SERVICE_STRING);
			owsMetadata.getServiceIdentification().setServiceType(serviceType);
			
			// check and set service protocol versions
			if (!owsMetadata.getServiceIdentification().isSetServiceTypeVersion())
				owsMetadata.getServiceIdentification().setServiceTypeVersion(Constants.SUPPORTED_WFS_VERSIONS);
			
			else {
				List<String> versions = owsMetadata.getServiceIdentification().getServiceTypeVersion();
				versions.removeIf(version -> !Constants.SUPPORTED_WFS_VERSIONS.contains(version));

				if (!versions.isEmpty())
					versions.sort(Collections.reverseOrder());
				else
					versions = Constants.SUPPORTED_WFS_VERSIONS;

				Constants.DEFAULT_WFS_VERSION = versions.get(0);
			}
		}

		super.afterUnmarshal(target, parent);
	}

}
