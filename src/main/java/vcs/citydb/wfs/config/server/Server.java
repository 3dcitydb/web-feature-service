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
    @XmlSchemaType(name = "positiveInteger")
    private Integer responseCacheTimeout = 300;
    private Boolean enableCORS = true;
    private String timeZone;
    private String importBasePath;
    private String textureServiceURL;
    private TextureCache textureCache;
    private Security security;
    private Cache tempCache;

    @XmlTransient
    private Path tempDir;

    public Server() {
        textureCache = new TextureCache();
        security = new Security();
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

    public int getResponseCacheTimeout() {
        return responseCacheTimeout;
    }

    public void setCacheTimeout(int responseCacheTimeout) {
        this.responseCacheTimeout = responseCacheTimeout > 0 ? responseCacheTimeout : 300;
    }

    public boolean isEnableCORS() {
        return enableCORS;
    }

    public void setEnableCORS(boolean enableCORS) {
        this.enableCORS = enableCORS;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public boolean isSetTimeZone() {
        return timeZone != null && !timeZone.isEmpty();
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getImportBasePath() {
        return importBasePath;
    }

    public boolean isSetImportBasePath() {
        return importBasePath != null && !importBasePath.isEmpty();
    }

    public void setImportBasePath(String importBasePath) {
        this.importBasePath = importBasePath;
    }

    public String getTextureServiceURL() {
        return textureServiceURL;
    }

    public boolean isSetTextureServiceURL() {
        return textureServiceURL != null && !textureServiceURL.isEmpty();
    }

    public void setTextureServiceURL(String textureServiceURL) {
        this.textureServiceURL = textureServiceURL;
    }

    public TextureCache getTextureCache() {
        return textureCache;
    }

    public void setTextureCache(TextureCache textureCache) {
        this.textureCache = textureCache;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
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
