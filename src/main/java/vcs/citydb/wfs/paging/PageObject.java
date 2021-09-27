package vcs.citydb.wfs.paging;

import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.GetPropertyValueType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.operation.getfeature.QueryExpression;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class PageObject {
    private final String identifier;
    private final long[] identifierBits;
    private final Path tempFile;
    private PageRequest pageRequest;
    private boolean serialized;
    private long cacheTime;

    private PageObject(String identifier, long[] identifierBits, WFSConfig wfsConfig) {
        this.identifier = identifier;
        this.identifierBits = identifierBits;
        tempFile = wfsConfig.getServer().getTempDir().resolve("paging-" + UUID.randomUUID().toString() + ".tmp");
    }

    PageObject(String identifier, long[] identifierBits, GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, WFSConfig wfsConfig) {
        this(identifier, identifierBits, wfsConfig);
        pageRequest = new GetFeatureRequest(wfsRequest, queryExpressions, wfsConfig, this);
    }

    PageObject(String identifier, long[] identifierBits, GetPropertyValueType wfsRequest, vcs.citydb.wfs.operation.getpropertyvalue.QueryExpression queryExpression, NamespaceFilter namespaceFilter, WFSConfig wfsConfig) {
        this(identifier, identifierBits, wfsConfig);
        pageRequest = new GetPropertyValueRequest(wfsRequest, queryExpression, namespaceFilter, wfsConfig, this);
    }

    String getIdentifier() {
        return identifier;
    }

    long[] getIdentifierBits() {
        return identifierBits;
    }

    PageRequest getBaseRequest() {
        return pageRequest;
    }

    PageRequest generateRequestFor(long pageNumber) {
        return pageRequest.generateRequestFor(pageNumber);
    }

    Path getTempFile() {
        return tempFile;
    }

    public boolean isSerialized() {
        return serialized;
    }

    public void setSerialized(boolean serialized) {
        this.serialized = serialized;
    }

    long getCacheTime() {
        return cacheTime;
    }

    void updateCacheTime() {
        cacheTime = System.nanoTime();
    }
}
