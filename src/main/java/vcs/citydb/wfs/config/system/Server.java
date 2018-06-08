package vcs.citydb.wfs.config.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ServiceType", propOrder={
		"externalServiceURL",
		"maxParallelRequests",
		"waitTimeout",
		"enableCORS"
})
public class Server {
	@XmlElement(required=true)
	private String externalServiceURL = "";
	private Integer maxParallelRequests = 30;
	private Integer waitTimeout = 60;
	private Boolean enableCORS = true;
	
	public String getExternalServiceURL() {
		return externalServiceURL;
	}

	public void setExternalServiceURL(String externalServiceURL) {
		this.externalServiceURL = externalServiceURL;
	}

	public int getMaxParallelRequests() {
		return maxParallelRequests;
	}
	
	public void setMaxParallelRequests(int maxParallelRequests) {
		if (maxParallelRequests <= 0)
			maxParallelRequests = 30;
		
		this.maxParallelRequests = maxParallelRequests;
	}
	
	public int getWaitTimeout() {
		return waitTimeout;
	}
	
	public void setWaitTimeout(int waitTimeout) {
		if (waitTimeout <= 0)
			waitTimeout = 60;
		
		this.waitTimeout = waitTimeout;
	}
	
	public boolean isEnableCORS() {
		return enableCORS;
	}

	public void setEnableCORS(boolean enableCORS) {
		this.enableCORS = enableCORS;
	}

}
