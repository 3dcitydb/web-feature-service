package vcs.citydb.wfs.config.server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "TextureCacheType", propOrder = {
        "localCachePath"
})
public class TextureCache {
    @XmlAttribute(required = true)
    private boolean isEnabled = false;
    private String localCachePath;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getLocalCachePath() {
        return localCachePath;
    }

    public boolean isSetLocalCachePath() {
        return localCachePath != null && !localCachePath.trim().isEmpty();
    }

    public void setLocalCachePath(String localCachePath) {
        this.localCachePath = localCachePath;
    }

}
