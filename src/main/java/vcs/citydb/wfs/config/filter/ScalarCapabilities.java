package vcs.citydb.wfs.config.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@XmlType(name = "ScalarCapabilitiesType", propOrder = {
        "logicalOperators",
        "comparisonOperators"
})
public class ScalarCapabilities {
    private Boolean logicalOperators;
    @XmlElementWrapper(name = "comparisonOperators")
    @XmlElement(name = "operator")
    private List<ComparisonOperatorName> comparisonOperators;

    public ScalarCapabilities() {
        comparisonOperators = new ArrayList<ComparisonOperatorName>();
    }

    public boolean isSetLogicalOperators() {
        return logicalOperators != null && logicalOperators;
    }

    public void enableLogicalOperators(boolean enable) {
        logicalOperators = enable;
    }

    public boolean isSetComparisonOperators() {
        return comparisonOperators != null && !comparisonOperators.isEmpty();
    }

    public void addComparisonOperator(ComparisonOperatorName comparisonOperator) {
        if (!comparisonOperators.contains(comparisonOperator))
            comparisonOperators.add(comparisonOperator);
    }

    public List<ComparisonOperatorName> getComparisonOperators() {
        return comparisonOperators;
    }

    public boolean containsComparisonOperator(ComparisonOperatorName comparisonOperator) {
        return comparisonOperators.contains(comparisonOperator);
    }

    public void setComparisonOperators(List<ComparisonOperatorName> comparisonOperators) {
        this.comparisonOperators = comparisonOperators;
    }

    public boolean containsAll(EnumSet<ComparisonOperatorName> comparisonOperators) {
        return this.comparisonOperators.containsAll(comparisonOperators);
    }

}
