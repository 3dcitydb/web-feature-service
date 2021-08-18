package vcs.citydb.wfs.config.server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "SecurityType", propOrder = {
        "accessControls"
})
public class Security {
    @XmlAttribute(required = true)
    private boolean isEnabled = false;
    @XmlElement(name = "accessControl")
    private List<AccessControl> accessControls;

    public Security() {
        accessControls = new ArrayList<>();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public List<AccessControl> getAccessControls() {
        return accessControls;
    }

    public void setAccessControls(List<AccessControl> accessControls) {
        this.accessControls = accessControls;
    }
}
