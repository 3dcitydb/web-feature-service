package vcs.citydb.wfs.config.filter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "FilterCapabilitiesType", propOrder = {
        "scalarCapabilities",
        "spatialCapabilities"
})
public class FilterCapabilities {
    private ScalarCapabilities scalarCapabilities;
    private SpatialCapabilities spatialCapabilities;

    public FilterCapabilities() {
        scalarCapabilities = new ScalarCapabilities();
        spatialCapabilities = new SpatialCapabilities();
    }

    public boolean isSetScalarCapabilities() {
        return scalarCapabilities != null
                && (scalarCapabilities.isSetLogicalOperators() || scalarCapabilities.isSetComparisonOperators());
    }

    public ScalarCapabilities getScalarCapabilities() {
        return scalarCapabilities;
    }

    public void setScalarCapabilities(ScalarCapabilities scalarCapabilities) {
        this.scalarCapabilities = scalarCapabilities;
    }

    public boolean isSetSpatialCapabilities() {
        return spatialCapabilities.getSpatialOperators() != null && !spatialCapabilities.getSpatialOperators().isEmpty();
    }

    public SpatialCapabilities getSpatialCapabilities() {
        return spatialCapabilities;
    }

    public void setSpatialCapabilities(SpatialCapabilities spatialCapabilities) {
        this.spatialCapabilities = spatialCapabilities;
    }

}
