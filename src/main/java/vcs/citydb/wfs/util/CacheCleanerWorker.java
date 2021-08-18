package vcs.citydb.wfs.util;

import org.citydb.util.concurrent.DefaultWorker;
import org.citydb.util.log.Logger;

public class CacheCleanerWorker extends DefaultWorker<CacheCleanerWork> {
	private final Logger log = Logger.getInstance();
	
	@Override
	public void doWork(CacheCleanerWork work) {
		try {
			work.run();
		} catch (Exception e) {
			log.error("Failed to clean cache: " + e.getMessage());
		}
	}

	@Override
	public void shutdown() {
		// nothing to do
	}
}
