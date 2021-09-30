package vcs.citydb.wfs.texture;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.stream.Stream;

@Path("/cache")
public class DeleteTexture {
	@Inject
	private TextureProvider textureProvider;

	@DELETE
	public void deleteCache() {
		java.nio.file.Path cachePath = textureProvider.getLocalCachePath();

		if (cachePath != null) {
			boolean useCache = textureProvider.isUseTextureCache();

			try {
				// disable caching and delete temp files
				textureProvider.setUseTextureCache(false);

				try (Stream<java.nio.file.Path> stream = Files.walk(cachePath)) {
					stream.filter(p -> !p.equals(cachePath))
							.sorted(Comparator.reverseOrder())
							.map(java.nio.file.Path::toFile)
							.forEach(File::delete);
				}
			} catch (IOException e) {
				throw new WebApplicationException("Failed to delete texture cache at '" + cachePath + "'.", e);
			} finally {
				// restore previous state
				textureProvider.setUseTextureCache(useCache);
			}
		}
	}
}
