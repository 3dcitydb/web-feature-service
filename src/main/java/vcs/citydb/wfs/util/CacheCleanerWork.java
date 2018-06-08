package vcs.citydb.wfs.util;

import org.citydb.citygml.common.database.cache.CacheTableManager;

import java.nio.file.Path;

public class CacheCleanerWork {
    private final CacheTableManager cacheTableManager;
    private final Path tempFile;

    private CacheCleanerWork(CacheTableManager cacheTableManager, Path tempFile) {
        this.cacheTableManager = cacheTableManager;
        this.tempFile = tempFile;
    }

    public CacheCleanerWork(CacheTableManager cacheTableManager) {
        this(cacheTableManager, null);
    }

    public CacheCleanerWork(Path tempFile) {
        this(null, tempFile);
    }

    public CacheTableManager getCacheTableManager() {
        return cacheTableManager;
    }

    public boolean isSetCacheTableManager() {
        return cacheTableManager != null;
    }

    public Path getTempFile() {
        return tempFile;
    }

    public boolean isSetTempFile() {
        return tempFile != null;
    }
}
