package vcs.citydb.wfs.config;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.Unmarshaller.Listener;

import net.opengis.ows._1.CodeType;
import vcs.citydb.wfs.config.capabilities.OWSMetadata;

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
				
				Iterator<String> iter = versions.iterator();
				while (iter.hasNext()) {
					if (!Constants.SUPPORTED_WFS_VERSIONS.contains(iter.next()))
						iter.remove();
				}
				
				if (!versions.isEmpty())
					Collections.sort(versions, Collections.reverseOrder());
				else
					versions = Constants.SUPPORTED_WFS_VERSIONS;
				
				Constants.DEFAULT_WFS_VERSION = versions.get(0);
			}
		}

		super.afterUnmarshal(target, parent);
	}

}
