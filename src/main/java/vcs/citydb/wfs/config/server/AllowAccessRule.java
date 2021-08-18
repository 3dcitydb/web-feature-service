package vcs.citydb.wfs.config.server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

@XmlType(name = "AllowAccessRuleType")
public class AllowAccessRule extends AbstractAccessRule {
    @XmlAttribute(name = "token")
    private Set<String> tokens;

    public AllowAccessRule() {
        tokens = new HashSet<>();
    }

    public Set<String> getTokens() {
        return tokens;
    }

    public void setTokens(Set<String> tokens) {
        this.tokens = tokens;
    }
}
