package vcs.citydb.wfs.config.server;

import vcs.citydb.wfs.config.operation.WFSOperation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

@XmlType(name = "AccessScopeType")
@XmlSeeAlso({
        AllowAccessRule.class,
        DenyAccessRule.class
})
public class AccessScope {
    @XmlAttribute(name = "operation", required = true)
    private Set<WFSOperation> operations;

    public AccessScope() {
        operations = new HashSet<>();
    }

    public Set<WFSOperation> getOperations() {
        return operations;
    }

    public void setOperations(Set<WFSOperation> operations) {
        this.operations = operations;
    }
}
