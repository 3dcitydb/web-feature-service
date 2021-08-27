package vcs.citydb.wfs.util;

import org.citydb.util.concurrent.DefaultWorker;

public class NullWorker<T> extends DefaultWorker<T> {

	@Override
	public void doWork(T work) {
		// do nothing
	}

	@Override
	public void shutdown() {
		// do nothing
	}

}
