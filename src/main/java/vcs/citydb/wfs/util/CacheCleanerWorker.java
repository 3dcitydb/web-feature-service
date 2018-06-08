package vcs.citydb.wfs.util;

import org.citydb.concurrent.DefaultWorker;
import org.citydb.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

public class CacheCleanerWorker extends DefaultWorker<CacheCleanerWork> {
	private final Logger log = Logger.getInstance();
	
	@Override
	public void doWork(CacheCleanerWork work) {
		if (work.isSetCacheTableManager()) {
			try {
				work.getCacheTableManager().dropAll();
			} catch (SQLException e) {
				log.error("Failed to clean temporary cache: " + e.getMessage());
			}
		}

		else if (work.isSetTempFile()) {
			try {
				Files.delete(work.getTempFile());
			} catch (IOException e) {
				log.error("Failed to clean temporary file: " + e.getMessage());
			}
		}
	}

	@Override
	public void shutdown() {
		// nothing to do
	}

}
