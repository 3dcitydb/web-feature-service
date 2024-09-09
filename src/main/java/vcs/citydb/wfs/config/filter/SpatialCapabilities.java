package vcs.citydb.wfs.config.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@XmlType(name = "SpatialCapabilitiesType", propOrder = {
        "operators"
})
public class SpatialCapabilities {
    @XmlElement(name = "operator")
    private List<SpatialOperatorName> operators;

    public SpatialCapabilities() {
        operators = new ArrayList<SpatialOperatorName>();
    }

    public void addSpatialOperator(SpatialOperatorName spatialOperator) {
        if (!operators.contains(spatialOperator))
            operators.add(spatialOperator);
    }

    public List<SpatialOperatorName> getSpatialOperators() {
        return operators;
    }

    public boolean containsSpatialOperator(SpatialOperatorName spatialOperator) {
        return operators.contains(spatialOperator);
    }

    public void setSpatialOperators(List<SpatialOperatorName> spatialOperators) {
        this.operators = spatialOperators;
    }

    public boolean containsAll(EnumSet<SpatialOperatorName> spatialOperators) {
        return operators.containsAll(spatialOperators);
    }

}
