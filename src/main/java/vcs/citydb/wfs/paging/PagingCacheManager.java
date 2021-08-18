package vcs.citydb.wfs.paging;

import net.opengis.wfs._2.BaseRequestType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.GetPropertyValueType;
import org.citydb.util.concurrent.WorkerPool;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.operation.getfeature.QueryExpression;
import vcs.citydb.wfs.util.CacheCleanerWork;
import vcs.citydb.wfs.xml.NamespaceFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PagingCacheManager {
    private final int MAINTENANCE_COUNT = 1000;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, PageObject> pageObjects = new ConcurrentHashMap<>();
    private final WorkerPool<CacheCleanerWork> cacheCleanerPool;
    private final WFSConfig wfsConfig;

    private final Pattern pageIdPattern = Pattern.compile("^([0-9a-f]{1,16})-([0-9a-f]{1,16})-([0-9a-f]{1,16})$");
    private final long maxLifeTime;

    public PagingCacheManager(WorkerPool<CacheCleanerWork> cacheCleanerPool, WFSConfig wfsConfig) {
        this.cacheCleanerPool = cacheCleanerPool;
        this.wfsConfig = wfsConfig;
        maxLifeTime = TimeUnit.SECONDS.toNanos(wfsConfig.getServer().getResponseCacheTimeout());
    }

    public PageRequest create(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions) {
        String key = getOrCreateIdentifier(wfsRequest);
        return create(key, v -> new PageObject(key, getIdentifierBits(key), wfsRequest, queryExpressions, wfsConfig));
    }

    public PageRequest create(GetPropertyValueType wfsRequest, vcs.citydb.wfs.operation.getpropertyvalue.QueryExpression queryExpression, NamespaceFilter namespaceFilter) {
        String key = getOrCreateIdentifier(wfsRequest);
        return create(key, v -> new PageObject(key, getIdentifierBits(key), wfsRequest, queryExpression, namespaceFilter, wfsConfig));
    }

    private PageRequest create(String key, Function<String, PageObject> mapping) {
        // trigger cache maintenance
        if (pageObjects.size() > MAINTENANCE_COUNT)
            cacheCleanerPool.addWork(this::cleanUp);

        return pageObjects.computeIfAbsent(key, mapping).getBaseRequest();
    }

    public PageRequest getIfValid(String pageId) {
        long accessTime = System.nanoTime();
        Matcher matcher = pageIdPattern.matcher(pageId);
        if (matcher.matches()) {
            String high = matcher.group(1);
            String low = matcher.group(2);
            PageObject pageObject = pageObjects.get(high + "-" + low);
            if (pageObject != null) {
                if (accessTime - pageObject.getCacheTime() <= maxLifeTime) {
                    pageObject.updateCacheTime();
                    long pageNumber = getPageNumber(high, low, matcher.group(3));
                    return pageObject.generateRequestFor(pageNumber);
                }

                remove(pageObject);
            }
        }

        return null;
    }

    public void update(PageRequest pageRequest) throws IOException {
        PageObject pageObject = pageObjects.get(pageRequest.getIdentifier());
        if (pageObject != null) {
            pageRequest.cacheValues();
            pageObject.updateCacheTime();
        }
    }

    public void remove(PageRequest pageRequest) {
        PageObject pageObject = pageObjects.get(pageRequest.getIdentifier());
        if (pageObject != null)
            remove(pageObject);
    }

    private void remove(PageObject pageObject) {
        pageObjects.remove(pageObject.getIdentifier());
        if (pageObject.isSerialized())
            cacheCleanerPool.addWork(() -> Files.delete(pageObject.getTempFile()));
    }

    public void cleanUp() {
        long accessTime = System.nanoTime();
        pageObjects.values().stream()
                .filter(p -> p.isSerialized() && accessTime - p.getCacheTime() > maxLifeTime)
                .collect(Collectors.toList())
                .forEach(this::remove);
    }

    public boolean isValidPageId(String pageId) {
        return pageIdPattern.matcher(pageId).matches();
    }

    private String getOrCreateIdentifier(BaseRequestType wfsRequest) {
        if (!wfsRequest.isSetIdentifier()) {
            long[] value = random.longs(2).toArray();
            wfsRequest.setIdentifier(Long.toHexString(value[0]) + "-" + Long.toHexString(value[1]));
        }

        return wfsRequest.getIdentifier();
    }

    private long getPageNumber(String high, String low, String pageNumber) {
        return Long.parseUnsignedLong(high, 16) ^ Long.parseUnsignedLong(low, 16) ^ Long.parseUnsignedLong(pageNumber, 16);
    }

    private long[] getIdentifierBits(String identifier) {
        String[] parts = identifier.split("-");
        return new long[] { Long.parseUnsignedLong(parts[0], 16), Long.parseUnsignedLong(parts[1], 16) };
    }
}
