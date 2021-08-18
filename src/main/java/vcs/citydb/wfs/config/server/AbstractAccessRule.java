package vcs.citydb.wfs.config.server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

@XmlType(name = "AbstractAccessRuleType")
@XmlSeeAlso({
        AllowAccessRule.class,
        DenyAccessRule.class
})
public abstract class AbstractAccessRule {
    @XmlAttribute(name = "ip")
    private Set<String> ipAddresses;

    public AbstractAccessRule() {
        ipAddresses = new HashSet<>();
    }

    public Set<String> getIPAddresses() {
        return ipAddresses;
    }

    public void setIPAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }
}
