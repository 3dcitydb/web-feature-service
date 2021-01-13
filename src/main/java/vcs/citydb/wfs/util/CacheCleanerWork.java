package vcs.citydb.wfs.util;

@FunctionalInterface
public interface CacheCleanerWork {
    void run() throws Exception;
}
