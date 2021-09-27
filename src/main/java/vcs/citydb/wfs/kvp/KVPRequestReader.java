package vcs.citydb.wfs.kvp;

import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.NamespacesParser;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import java.util.Map;

public abstract class KVPRequestReader {
	protected final Map<String, String> parameters;
	protected final WFSConfig wfsConfig;

	private NamespaceFilter namespaceFilter;

	public KVPRequestReader(Map<String, String> parameters, WFSConfig wfsConfig) {
		this.parameters = parameters;
		this.wfsConfig = wfsConfig;
	}

	public abstract Object readRequest() throws WFSException;
	public abstract String getOperationName();

	public NamespaceFilter getNamespaces() throws WFSException {
		if (namespaceFilter == null) {
			try {
				namespaceFilter = new NamespacesParser(wfsConfig).parse(KVPConstants.NAMESPACES, parameters.get(KVPConstants.NAMESPACES));
			} catch (KVPParseException e) {
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getParameter(), e.getCause());
			}
		}

		return namespaceFilter;
	}
}
