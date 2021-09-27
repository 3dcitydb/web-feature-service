package vcs.citydb.wfs.texture;

import java.nio.file.Path;

public class TextureWrapper {
	private final byte[] bytes;
	private final String mimeType;
	private Path path;
	
	public TextureWrapper(byte[] bytes, String mimeType) {
		this.bytes = bytes;
		this.mimeType = mimeType;
	}

	public TextureWrapper(Path path, String mimeType) {
		this.path = path;
		this.mimeType = mimeType;
		bytes = null;
	}

	public boolean isSetBytes() {
		return bytes != null;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getMimeType() {
		return mimeType;
	}

	public boolean isSetPath() {
		return path != null;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
	
}
