package vcs.citydb.wfs.texture;

import org.apache.tika.Tika;
import org.citydb.config.Config;
import org.citydb.core.database.adapter.BlobExportAdapter;
import org.citydb.core.database.adapter.BlobType;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.util.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.log.Logger;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.util.DatabaseConnector;
import vcs.citydb.wfs.util.RequestLimiter;
import vcs.citydb.wfs.util.TextureCacheWorker;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class TextureProvider {
	private final Logger log = Logger.getInstance();
	private final DatabaseConnectionPool connectionPool;
	private final Tika mimeTypes;
	private final WorkerPool<TextureWrapper> textureCachePool;
	private final RequestLimiter limiter;
	private final WFSConfig wfsConfig;
	private final Config config;

	private Path cachePath;

	@Inject
	public TextureProvider(ServletContext context, WFSConfig wfsConfig, Config config) {
		this.wfsConfig = wfsConfig;
		this.config = config;

		connectionPool = DatabaseConnectionPool.getInstance();
		mimeTypes = new Tika();
		limiter = ObjectRegistry.getInstance().lookup(RequestLimiter.class);

		if (isUseTextureCache()) {
			if (wfsConfig.getServer().getTextureCache().isSetLocalCachePath()) {
				File tmp = new File(wfsConfig.getServer().getTextureCache().getLocalCachePath());
				if (tmp.isDirectory() && tmp.canWrite())
					cachePath = Paths.get(wfsConfig.getServer().getTextureCache().getLocalCachePath());
				else {
					log.error("Invalid path for texture cache: '" + tmp.getAbsolutePath() + "'.");
					log.error("Texture caching is disabled.");
					setUseTextureCache(false);
				}
			} else
				cachePath = Paths.get(context.getRealPath(Constants.TEXTURE_CACHE_PATH));
		}
		
		if (isUseTextureCache()) {
			textureCachePool = new WorkerPool<>(
					"texture_cache",
					5,
					5,
					PoolSizeAdaptationStrategy.AGGRESSIVE,
					() -> new TextureCacheWorker(TextureProvider.this),
					1);

			textureCachePool.prestartCoreWorkers();
			log.debug("Using texture cache at '" + cachePath.toString() + "'.");
		} else {
			textureCachePool = null;
			log.debug("Texture caching is disabled.");
		}
	}

	public void addTextureToCache(TextureWrapper texture, String name, String bucket) {
		texture.setPath(Paths.get(cachePath.toString(), bucket, name));
		textureCachePool.addWork(texture);
	}

	public TextureWrapper getFromCache(String name, String bucket) {
		Path target = Paths.get(cachePath.toString(), bucket, name);
		return Files.isReadable(target) ? new TextureWrapper(target, mimeTypes.detect(name)) : null;
	}

	public TextureWrapper getFromDatabase(String name, HttpServletRequest request) throws WFSException, SQLException {
		// make sure we only serve a maximum number of requests in parallel
		limiter.requireServiceSlot(request);

		BlobExportAdapter adapter = null;
		boolean purgeConnectionPool = false;

		try (Connection connection = initConnection()) {
			// extract database id from texture name
			int pos = name.indexOf('.');
			long textureId = Long.parseLong(name.substring(CoreConstants.UNIQUE_TEXTURE_FILENAME_PREFIX.length(), pos != -1 ? pos : name.length()));

			// read texture image
			adapter = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().getBlobExportAdapter(connection, BlobType.TEXTURE_IMAGE);

			byte[] bytes = adapter.getInByteArray(textureId);
			if (bytes == null)
				throw new SQLException("There is no texture image for id '" + textureId + "'.");

			return new TextureWrapper(bytes, mimeTypes.detect(name.toLowerCase()));
		} catch (NumberFormatException e) {
			throw new WebApplicationException(Response.serverError()
					.entity("Failed to parse texture file name '" + name + "'.")
					.type(MediaType.TEXT_PLAIN).build());
		} catch (SQLException e) {
			purgeConnectionPool = true;
			throw e;
		} finally {
			limiter.releaseServiceSlot(request);

			if (adapter != null) {
				try {
					adapter.close();
				} catch (SQLException e) {
					//
				}
			}

			// purge connection pool to remove possibly defect connections
			if (purgeConnectionPool)
				connectionPool.purge();
		}
	}

	public boolean isUseTextureCache() {
		return wfsConfig.getConstraints().isExportAppearance() && wfsConfig.getServer().getTextureCache().isEnabled();
	}

	public void setUseTextureCache(boolean enable) {
		wfsConfig.getServer().getTextureCache().setEnabled(enable);
	}
	
	public Path getLocalCachePath() {
		return cachePath;
	}

	public void destroy() {
		if (isUseTextureCache() && textureCachePool != null)
			textureCachePool.shutdown();
	}

	private Connection initConnection() throws WFSException, SQLException {
		// check database connection
		if (!connectionPool.isConnected())
			DatabaseConnector.connect(config);

		// get connection
		Connection connection = connectionPool.getConnection();
		connection.setAutoCommit(false);

		return connection;
	}
}
