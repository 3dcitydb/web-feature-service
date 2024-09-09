package vcs.citydb.wfs.config.constraints;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "WFSLodFilterType")
public class LodFilter extends org.citydb.config.project.query.filter.lod.LodFilter {
    @XmlAttribute(required = true)
    private boolean isEnabled = false;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
