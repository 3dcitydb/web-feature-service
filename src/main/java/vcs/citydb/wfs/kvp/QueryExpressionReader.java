package vcs.citydb.wfs.kvp;

import net.opengis.fes._2.AbstractQueryExpressionType;
import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.SortByType;
import net.opengis.wfs._2.ParameterType;
import net.opengis.wfs._2.PropertyName;
import net.opengis.wfs._2.QueryType;
import net.opengis.wfs._2.StoredQueryType;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.*;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.validation.Schema;
import java.util.*;

public class QueryExpressionReader {
	private final net.opengis.wfs._2.ObjectFactory wfsFactory;
	private final net.opengis.fes._2.ObjectFactory fesFactory;
	private final net.opengis.gml.ObjectFactory gmlFactory;
	private final Schema wfsSchema;
	private final CityGMLBuilder cityGMLBuilder;
	private final WFSConfig wfsConfig;

	public QueryExpressionReader(net.opengis.wfs._2.ObjectFactory wfsFactory,
			net.opengis.fes._2.ObjectFactory fesFactory,
			net.opengis.gml.ObjectFactory gmlFactory,
			Schema wfsSchema,
			CityGMLBuilder cityGMLBuilder, 
			WFSConfig wfsConfig) {
		this.wfsFactory = wfsFactory;
		this.fesFactory = fesFactory;
		this.gmlFactory = gmlFactory;
		this.wfsSchema = wfsSchema;
		this.cityGMLBuilder = cityGMLBuilder;
		this.wfsConfig = wfsConfig;
	}

	public List<JAXBElement<? extends AbstractQueryExpressionType>> read(Map<String, String> parameters, String operationName, NamespaceFilter namespaceFilter, boolean allowMultipleQueries) throws WFSException {
		List<JAXBElement<? extends AbstractQueryExpressionType>> queries = new ArrayList<>();
		
		try {
			// ensure mutual exclusivity
			checkMutualExclusivity(parameters, operationName, KVPConstants.FILTER, KVPConstants.RESOURCE_ID, KVPConstants.BBOX);

			List<List<String>> typeNames = null;
			List<FilterType> resourceIds = null;

			if (parameters.containsKey(KVPConstants.TYPE_NAMES))
				typeNames = new ValueListParser<>(new StringParser()).parse(KVPConstants.TYPE_NAMES, parameters.get(KVPConstants.TYPE_NAMES));

			if (parameters.containsKey(KVPConstants.RESOURCE_ID))
				resourceIds = new ResourceIdParser(fesFactory).parse(KVPConstants.RESOURCE_ID, parameters.get(KVPConstants.RESOURCE_ID));

			typeNames = checkTypeNames(typeNames, resourceIds);
			if (typeNames != null) {
				if (!allowMultipleQueries && typeNames.size() > 1)
					throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "The request may only take a single ad hoc query expression or stored query expression.");
				
				// ad-hoc query				
				checkAlignedListSize(typeNames.size(), parameters, operationName,
						KVPConstants.RESOURCE_ID, KVPConstants.ALIASES, KVPConstants.SRS_NAME, KVPConstants.PROPERTY_NAME, KVPConstants.FILTER, KVPConstants.FILTER_LANGUAGE, KVPConstants.SORT_BY);

				List<List<String>> aliases = null;
				List<String> srsNames = null;
				List<List<PropertyName>> propertyNames = null;
				List<FilterType> filters = null;
				List<String> filterLanguages;
				List<SortByType> sortBys = null;

				if (parameters.containsKey(KVPConstants.STOREDQUERY_ID))
					throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "The request may either contain ad hoc query expression(s) or a stored query expression but not both.");

				if (parameters.containsKey(KVPConstants.ALIASES))
					aliases = new ValueListParser<>(new StringParser()).parse(KVPConstants.ALIASES, parameters.get(KVPConstants.ALIASES));

				if (parameters.containsKey(KVPConstants.SRS_NAME))
					srsNames = new SingleValueListParser<>(new StringParser()).parse(KVPConstants.SRS_NAME, parameters.get(KVPConstants.SRS_NAME));

				if (parameters.containsKey(KVPConstants.PROPERTY_NAME))
					propertyNames = new ValueListParser<>(new PropertyNameParser(namespaceFilter)).parse(KVPConstants.PROPERTY_NAME, parameters.get(KVPConstants.PROPERTY_NAME));

				if (parameters.containsKey(KVPConstants.FILTER_LANGUAGE)) {
					filterLanguages = new SingleValueListParser<>(new StringParser()).parse(KVPConstants.FILTER_LANGUAGE, parameters.get(KVPConstants.FILTER_LANGUAGE));
					for (String filterLanguage : filterLanguages) {
						if (!KVPConstants.DEFAULT_FILTER_LANGUAGE.equals(filterLanguage))
							throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Only the language '" + KVPConstants.DEFAULT_FILTER_LANGUAGE + "' is supported for filter expressions.", KVPConstants.FILTER_LANGUAGE);
					}
				}

				if (parameters.containsKey(KVPConstants.FILTER))
					filters = new SingleValueListParser<>(new FilterParser(namespaceFilter, wfsSchema, cityGMLBuilder, wfsConfig)).parse(KVPConstants.FILTER, parameters.get(KVPConstants.FILTER));

				if (parameters.containsKey(KVPConstants.SORT_BY))
					sortBys = new SortByParser().parse(KVPConstants.SORT_BY, parameters.get(KVPConstants.SORT_BY));

				FilterType bbox = null;
				if (parameters.containsKey(KVPConstants.BBOX))
					bbox = new BBoxParser(fesFactory, gmlFactory).parse(KVPConstants.BBOX, parameters.get(KVPConstants.BBOX));

				for (int i = 0; i < typeNames.size(); i++) {
					QueryType query = new QueryType();

					query.getTypeNames().addAll(typeNames.get(i));

					if (aliases != null)
						query.getAliases().addAll(aliases.get(i));

					if (srsNames != null)
						query.setSrsName(srsNames.get(i));

					// projection
					if (propertyNames != null) {
						List<JAXBElement<?>> jaxbElements = new ArrayList<>();
						for (PropertyName propertyName : propertyNames.get(i))
							jaxbElements.add(wfsFactory.createPropertyName(propertyName));

						query.setAbstractProjectionClause(jaxbElements);
					}

					// selection
					if (resourceIds != null)
						query.setAbstractSelectionClause(fesFactory.createAbstractSelectionClause(resourceIds.get(i)));					
					else if (bbox != null)
						query.setAbstractSelectionClause(fesFactory.createAbstractSelectionClause(bbox));
					else if (filters != null)
						query.setAbstractSelectionClause(fesFactory.createAbstractSelectionClause(filters.get(i)));

					// sorting
					if (sortBys != null)
						query.setAbstractSortingClause(fesFactory.createSortBy(sortBys.get(i)));

					queries.add(wfsFactory.createQuery(query));
				}

			} else {
				// stored query
				String storedQueryId = null;
				if (parameters.containsKey(KVPConstants.STOREDQUERY_ID))
					storedQueryId = new StringParser().parse(KVPConstants.STOREDQUERY_ID, parameters.get(KVPConstants.STOREDQUERY_ID));

				if (storedQueryId == null)
					throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The query request lacks the mandatory parameter " + KVPConstants.STOREDQUERY_ID + ".");

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
			}

		} catch (KVPParseException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getParameter(), e.getCause());
		}
		
		return queries;
	}

	private void checkMutualExclusivity(Map<String, String> parameters, String operationName, String... keys) throws WFSException {
		String found = null;
		for (String key : keys) {
			if (parameters.containsKey(key)) {
				if (found == null)
					found = key;

				else throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "The parameters " + found + " and " + key + " are mutually exclusive.", operationName);
			}
		}
	}

	private List<List<String>> checkTypeNames(List<List<String>> typeNames, List<FilterType> resourceIds) {
		if (typeNames == null && resourceIds != null) {
			typeNames = new ArrayList<>(resourceIds.size());
			for (int i = 0; i < resourceIds.size(); i++) {
				CityGMLModule core = wfsConfig.getFeatureTypes().getDefaultVersion().getCityGMLModule(CityGMLModuleType.CORE);
				typeNames.add(Collections.singletonList("schema-element(" + core.getNamespacePrefix() + ":_CityObject)"));
			}
		}

		return typeNames;
	}

	private void checkAlignedListSize(int size, Map<String, String> parameters, String operationName, String... keys) throws WFSException {
		for (String key : keys) {
			if (parameters.containsKey(key)) {
				String[] lists = parameters.get(key).split(KVPConstants.LIST_DELIMITER); 
				if (lists.length != size)
					throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "The query uses parameter lists whose sizes are not aligned.", operationName);
			}
		}
	}
}
