package vcs.citydb.wfs.kvp;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.wfs._2.ParameterType;
import net.opengis.wfs._2.StoredQueryType;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.StringParser;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueryExpressionReader {
	private final net.opengis.wfs._2.ObjectFactory wfsFactory;

	public QueryExpressionReader(net.opengis.wfs._2.ObjectFactory wfsFactory) {
		this.wfsFactory = wfsFactory;
	}

	public List<JAXBElement<? extends AbstractQueryExpressionType>> read(Map<String, String> parameters, String operationName, NamespaceFilter namespaceFilter, boolean allowMultipleQueries) throws WFSException {
		List<JAXBElement<? extends AbstractQueryExpressionType>> queries = new ArrayList<>();
		
		try {
			// stored query
			String storedQueryId = null;
			if (parameters.containsKey(KVPConstants.STOREDQUERY_ID))
				storedQueryId = new StringParser().parse(KVPConstants.STOREDQUERY_ID, parameters.get(KVPConstants.STOREDQUERY_ID));

			if (storedQueryId == null)
				throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The query request lacks the mandatory parameter " + KVPConstants.TYPE_NAMES + ".");

			StoredQueryType storedQuery = new StoredQueryType();

			storedQuery.setId(storedQueryId);

			for (String key : parameters.keySet()) {
				if (KVPConstants.PARAMETERS.contains(key))
					continue;

				ParameterType parameter = new ParameterType();
				parameter.setName(key);
				parameter.setContent(Arrays.asList(new Object[]{parameters.get(key)}));
				storedQuery.getParameter().add(parameter);
			}

			queries.add(wfsFactory.createStoredQuery(storedQuery));
		} catch (KVPParseException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getParameter(), e.getCause());
		}
		
		return queries;
	}
}
