package vcs.citydb.wfs.config.conformance;

import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.filter.ComparisonOperatorName;
import vcs.citydb.wfs.config.filter.SpatialOperatorName;
import vcs.citydb.wfs.config.operation.EncodingMethod;

public class Conformance {
    private final WFSConfig wfsConfig;

    public Conformance(WFSConfig wfsConfig) {
        this.wfsConfig = wfsConfig;
    }

    public boolean implementsBasicWFS() {
        return wfsConfig.getOperations().getGetPropertyValue().isEnabled()
                && wfsConfig.getConstraints().isSupportAdHocQueries()
                && wfsConfig.getConstraints().isUseDefaultSorting()
                && wfsConfig.getFilterCapabilities().isSetSpatialCapabilities()
                && wfsConfig.getFilterCapabilities().getSpatialCapabilities().containsAll(SpatialOperatorName.MIN_SPATIAL_FILTER);
    }

    public boolean implementsTransactionalWFS() {
        return false;
    }

    public boolean implementsLockingWFS() {
        return false;
    }

    public boolean implementsKVPEncoding() {
        return wfsConfig.getOperations().getRequestEncoding().getMethod() != EncodingMethod.XML;
    }

    public boolean implementsXMLEncoding() {
        return wfsConfig.getOperations().getRequestEncoding().getMethod() != EncodingMethod.KVP;
    }

    public boolean implementsSOAPEncoding() {
        return false;
    }

    public boolean implementsInheritance() {
        return true;
    }

    public boolean implementsRemoteResolve() {
        return false;
    }

    public boolean implementsResultPaging() {
        return wfsConfig.getConstraints().isUseResultPaging();
    }

    public boolean implementsStandardJoins() {
        return false;
    }

    public boolean implementsSpatialJoins() {
        return false;
    }

    public boolean implementsTemporalJoins() {
        return false;
    }

    public boolean implementsFeatureVersioning() {
        return false;
    }

    public boolean implementsManageStoredQueries() {
        return wfsConfig.getOperations().getManagedStoredQueries().isEnabled();
    }

    public boolean implementsQuery() {
        return true;
    }

    public boolean implementsAdHocQuery() {
        return wfsConfig.getConstraints().isSupportAdHocQueries();
    }

    public boolean implementsFunctions() {
        return false;
    }

    public boolean implementsResourceld() {
        return true;
    }

    public boolean implementsMinStandardFilter() {
        return wfsConfig.getFilterCapabilities().isSetScalarCapabilities()
                && wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetLogicalOperators()
                && wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetComparisonOperators()
                && wfsConfig.getFilterCapabilities().getScalarCapabilities().containsAll(ComparisonOperatorName.MIN_STANDARD_FILTER);
    }

    public boolean implementsStandardFilter() {
        return wfsConfig.getFilterCapabilities().isSetScalarCapabilities()
                && wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetLogicalOperators()
                && wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetComparisonOperators()
                && wfsConfig.getFilterCapabilities().getScalarCapabilities().containsAll(ComparisonOperatorName.STANDARD_FILTER);
    }

    public boolean implementsMinSpatialFilter() {
        return wfsConfig.getFilterCapabilities().isSetSpatialCapabilities()
                && wfsConfig.getFilterCapabilities().getSpatialCapabilities().containsAll(SpatialOperatorName.MIN_SPATIAL_FILTER);
    }

    public boolean implementsSpatialFilter() {
        return implementsMinSpatialFilter()
                && wfsConfig.getFilterCapabilities().getSpatialCapabilities().getSpatialOperators().size() > 1;
    }

    public boolean implementsMinTemporalFilter() {
        return false;
    }

    public boolean implementsTemporalFilter() {
        return false;
    }

    public boolean implementsVersionNav() {
        return false;
    }

    public boolean implementsSorting() {
        return true;
    }

    public boolean implementsExtendedOperators() {
        return false;
    }

    public boolean implementsMinimumXPath() {
        return true;
    }

    public boolean implementsSchemaElementFunc() {
        return true;
    }
}
