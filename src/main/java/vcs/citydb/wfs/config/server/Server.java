package vcs.citydb.wfs.config.server;

import org.citydb.config.project.global.Cache;

import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.nio.file.Path;

@XmlType(name = "ServerType", propOrder = {})
public class Server {
    private String externalServiceURL;
    @XmlSchemaType(name = "nonNegativeInteger")
    private Integer maxParallelRequests = 30;
    @XmlSchemaType(name = "positiveInteger")
    private Integer waitTimeout = 60;
	private Boolean enableCORS = true;
    private Cache tempCache;

    @XmlTransient
    private Path tempDir;

    public Server() {
        tempCache = new Cache();
    }

    public String getExternalServiceURL() {
        return externalServiceURL;
    }

    public boolean isSetExternalServiceURL() {
        return externalServiceURL != null && !externalServiceURL.isEmpty();
    }

    public void setExternalServiceURL(String externalServiceURL) {
        this.externalServiceURL = externalServiceURL;
    }

    public int getMaxParallelRequests() {
        return maxParallelRequests;
    }

    public void setMaxParallelRequests(int maxParallelRequests) {
        this.maxParallelRequests = maxParallelRequests >= 0 ? maxParallelRequests : 30;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout > 0 ? waitTimeout : 60;
    }
	
	public boolean isEnableCORS() {
		return enableCORS;
	}

	public void setEnableCORS(boolean enableCORS) {
		this.enableCORS = enableCORS;
	}

    public Cache getTempCache() {
        return tempCache;
    }

    public void setTempCache(Cache uidCache) {
        this.tempCache = uidCache;
    }

    public Path getTempDir() {
        return tempDir;
    }

    public void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
    }
}
