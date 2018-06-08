package vcs.citydb.wfs.config.conformance;

import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.EncodingMethod;

public class Conformance {
	private final WFSConfig wfsConfig;

	public Conformance(WFSConfig wfsConfig) {
		this.wfsConfig = wfsConfig;
	}

	public boolean implementsBasicWFS() {
		return false;
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
		return false;
	}

	public boolean implementsRemoteResolve() {
		return false;
	}

	public boolean implementsResultPaging() {
		return false;
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
		return false;
	}

	public boolean implementsQuery() {
		return true;
	}

	public boolean implementsAdHocQuery() {
		return false;
	}

	public boolean implementsFunctions() {
		return false;
	}

	public boolean implementsResourceld() {
		return false;
	}

	public boolean implementsMinStandardFilter() {
		return false;
	}

	public boolean implementsStandardFilter() {
		return false;
	}

	public boolean implementsMinSpatialFilter() {
		return false;
	}

	public boolean implementsSpatialFilter() {
		return false;
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
		return false;
	}

	public boolean implementsExtendedOperators() {
		return false;
	}

	public boolean implementsMinimumXPath() {
		return false;
	}

	public boolean implementsSchemaElementFunc() {
		return false;
	}
}
