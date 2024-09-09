package vcs.citydb.wfs.config.constraints;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "ResourceIdType")
public class ResourceId {
    @XmlAttribute
    private String prefix;
    @XmlValue
    private boolean replace;

    public String getPrefix() {
        return prefix != null ? prefix : "ID_";
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }
}
