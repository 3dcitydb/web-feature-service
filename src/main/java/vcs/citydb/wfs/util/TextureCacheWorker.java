package vcs.citydb.wfs.util;

import org.citydb.util.concurrent.DefaultWorker;
import org.citydb.util.log.Logger;
import vcs.citydb.wfs.texture.TextureProvider;
import vcs.citydb.wfs.texture.TextureWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextureCacheWorker extends DefaultWorker<TextureWrapper> {
    private final Logger log = Logger.getInstance();
    private final TextureProvider textureProvider;

    public TextureCacheWorker(TextureProvider textureProvider) {
        this.textureProvider = textureProvider;
    }

    @Override
    public void doWork(TextureWrapper work) {
        if (work.isSetBytes() && work.isSetPath()) {
            try {
                Path target = work.getPath();
                Files.createDirectories(target.getParent());
                Files.write(target, work.getBytes());
            } catch (IOException e) {
                log.error("Failed to write texture to cache: " + e.getMessage());
                log.error("Texture caching is disabled.");
                textureProvider.setUseTextureCache(false);
            }
        }
    }

    @Override
    public void shutdown() {
        // nothing to do
    }
}
