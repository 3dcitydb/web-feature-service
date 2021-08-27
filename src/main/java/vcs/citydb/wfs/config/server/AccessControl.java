package vcs.citydb.wfs.config.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "AccessControlType", propOrder = {
        "scopes",
        "rules"
})
public class AccessControl {
    @XmlElement(name = "scope")
    private List<AccessScope> scopes;
    @XmlElements({
            @XmlElement(name = "allow", type = AllowAccessRule.class),
            @XmlElement(name = "deny", type = DenyAccessRule.class)
    })
    private List<AbstractAccessRule> rules;

    public AccessControl() {
        scopes = new ArrayList<>();
        rules = new ArrayList<>();
    }

    public List<AccessScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<AccessScope> scopes) {
        this.scopes = scopes;
    }

    public List<AbstractAccessRule> getRules() {
        return rules;
    }

    public void setRules(List<AbstractAccessRule> rules) {
        this.rules = rules;
    }
}
