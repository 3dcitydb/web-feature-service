package vcs.citydb.wfs.operation.storedquery;

import java.nio.file.Path;

public class StoredQueryAdapter {
	private final String id;
	private final Path file;
	
	private StoredQueryAdapter(String id, Path file) {
		this.id = id;
		this.file = file;
	}
	
	protected StoredQueryAdapter(String id) {
		this(id, null);
	}
	
	protected StoredQueryAdapter(Path file) {
		this(null, file);
	}

	protected String getId() {
		return id;
	}

	protected boolean isSetId() {
		return id != null;
	}

	protected Path getFile() {
		return file;
	}
	
	protected boolean isSetFile() {
		return file != null;
	}
	
}
