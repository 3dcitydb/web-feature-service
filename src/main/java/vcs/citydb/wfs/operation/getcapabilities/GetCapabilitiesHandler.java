package vcs.citydb.wfs.operation.getcapabilities;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import net.opengis.fes._2.*;
import net.opengis.fes._2.GeometryOperandsType.GeometryOperand;
import net.opengis.ows._1.*;
import net.opengis.wfs._2.GetCapabilitiesType;
import net.opengis.wfs._2.*;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.util.Util;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.gml.GMLCoreModule;
import org.citygml4j.model.module.gml.XLinkModule;
import org.citygml4j.util.xml.SAXWriter;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.capabilities.OWSMetadata;
import vcs.citydb.wfs.config.conformance.Conformance;
import vcs.citydb.wfs.config.feature.FeatureType;
import vcs.citydb.wfs.config.filter.ComparisonOperatorName;
import vcs.citydb.wfs.config.filter.SpatialOperandName;
import vcs.citydb.wfs.config.filter.SpatialOperatorName;
import vcs.citydb.wfs.config.operation.OutputFormat;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.util.LoggerUtil;
import vcs.citydb.wfs.util.ServerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class GetCapabilitiesHandler {
	private final Logger log = Logger.getInstance();
	private final WFSConfig wfsConfig;

	private final Conformance conformance;
	private final Marshaller marshaller;
	private final net.opengis.wfs._2.ObjectFactory wfsFactory;
	private final net.opengis.ows._1.ObjectFactory owsFactory;
	private final AbstractDatabaseAdapter databaseAdapter;

	private final ValueType TRUE;
	private final ValueType FALSE;

	public GetCapabilitiesHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) throws JAXBException {
		this.wfsConfig = wfsConfig;

		conformance = new Conformance(wfsConfig);
		wfsFactory = new net.opengis.wfs._2.ObjectFactory();
		owsFactory = new net.opengis.ows._1.ObjectFactory();
		marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();

		TRUE = new ValueType();
		TRUE.setValue("TRUE");
		FALSE = new ValueType();
		FALSE.setValue("FALSE");
	}

	public void doOperation(GetCapabilitiesType wfsRequest,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {
		log.info(LoggerUtil.getLogMessage(request, "Accepting GetCapabilities request."));

		// check service attribute
		if (!wfsRequest.isSetService())
			throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The request lacks the mandatory " + KVPConstants.SERVICE + " parameter.", KVPConstants.SERVICE);
		else if(!Constants.WFS_SERVICE_STRING.equals(wfsRequest.getService()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'service' must match the fixed value '" + Constants.WFS_SERVICE_STRING + "'.", KVPConstants.SERVICE);

		// negotiate version
		String version = wfsConfig.getCapabilities().getSupportedWFSVersions().get(0);
		if (wfsRequest.isSetAcceptVersions() && wfsRequest.getAcceptVersions().isSetVersion()) {
			boolean success = false;
			for (String tmp : wfsRequest.getAcceptVersions().getVersion()) {
				if (wfsConfig.getCapabilities().getSupportedWFSVersions().contains(tmp)) {
					version = tmp;
					success = true;
					break;
				}
			}

			if (!success) {
				WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.VERSION_NEGOTIATION_FAILED);
				message.addExceptionText("None of the requested version numbers '" + Util.collection2string(wfsRequest.getAcceptVersions().getVersion(), ", ") + "' is supported by this WFS service implementation.");
				message.addExceptionText("Supported version numbers are '" + Util.collection2string(wfsConfig.getCapabilities().getSupportedWFSVersions(), ", ") + "'.");
				throw new WFSException(message);				
			}
		}

		// dynamically generate capabilities document
		WFS_CapabilitiesType capabilities = new WFS_CapabilitiesType();
		capabilities.setVersion(version);

		// service identification and provider are copied from configuration file
		OWSMetadata owsMetadata = wfsConfig.getCapabilities().getOwsMetadata();
		capabilities.setServiceIdentification(owsMetadata.getServiceIdentification());
		capabilities.setServiceProvider(owsMetadata.getServiceProvider());

		OperationsMetadata operations = new OperationsMetadata();
		FeatureTypeListType featureTypeList = new FeatureTypeListType();
		Filter_Capabilities filterCapabilities = new Filter_Capabilities();
		capabilities.setOperationsMetadata(operations);
		capabilities.setFeatureTypeList(featureTypeList);
		capabilities.setFilter_Capabilities(filterCapabilities);

		// add operations
		addOperations(operations, request);

		// add global parameter domains and constraints
		addParameterDomainsAndConstraints(operations);

		// add feature types
		addFeatureTypes(featureTypeList);

		// add filter capabilities
		addFilterCapabilities(filterCapabilities);

		// write response
		printCapabilitiesDocument(request, response, capabilities);

		log.info(LoggerUtil.getLogMessage(request, "GetCapabilities operation successfully finished."));
	}

	private void addOperations(OperationsMetadata operationsMetadata, HttpServletRequest request) throws WFSException {
		String serviceURL = wfsConfig.getServer().isSetExternalServiceURL() ?
				wfsConfig.getServer().getExternalServiceURL() :
				ServerUtil.getServiceURL(request);

		RequestMethodType requestMethod = new RequestMethodType();
		requestMethod.setHref(serviceURL + Constants.WFS_SERVICE_PATH);

		DCP getAndPost = new DCP();
		getAndPost.setHTTP(new HTTP());
		if (conformance.implementsKVPEncoding())
			getAndPost.getHTTP().getGetOrPost().add(owsFactory.createHTTPGet(requestMethod));
		if (conformance.implementsXMLEncoding())
			getAndPost.getHTTP().getGetOrPost().add(owsFactory.createHTTPPost(requestMethod));

		DCP post = new DCP();
		post.setHTTP(new HTTP());
		post.getHTTP().getGetOrPost().add(owsFactory.createHTTPPost(requestMethod));

		// GetCapabilities operation
		{
			Operation getCapabilities = new Operation();
			getCapabilities.setName(KVPConstants.GET_CAPABILITIES);		
			getCapabilities.getDCP().add(getAndPost);		
			operationsMetadata.getOperation().add(getCapabilities);

			DomainType acceptVersions = new DomainType();
			getCapabilities.getParameter().add(acceptVersions);
			acceptVersions.setName("AcceptVersions");
			acceptVersions.setAllowedValues(new AllowedValues());

			for (String version : wfsConfig.getCapabilities().getSupportedWFSVersions()) {
				ValueType value = new ValueType();
				value.setValue(version);
				acceptVersions.getAllowedValues().getValueOrRange().add(value);
			}

			DomainType acceptFormats = new DomainType();
			getCapabilities.getParameter().add(acceptFormats);
			acceptFormats.setName("AcceptFormats");
			acceptFormats.setAllowedValues(new AllowedValues());
			ValueType format = new ValueType();
			format.setValue("text/xml");
			acceptFormats.getAllowedValues().getValueOrRange().add(format);
		}

		// DescribeFeatureType operation
		{
			Operation describeFeatureType = new Operation();
			describeFeatureType.setName(KVPConstants.DESCRIBE_FEATURE_TYPE);		
			describeFeatureType.getDCP().add(getAndPost);
			operationsMetadata.getOperation().add(describeFeatureType);		

			DomainType outputFormat = new DomainType();
			describeFeatureType.getParameter().add(outputFormat);
			outputFormat.setName("outputFormat");
			outputFormat.setAllowedValues(new AllowedValues());
			for (OutputFormat format : wfsConfig.getOperations().getDescribeFeatureType().getOutputFormats()) {
				ValueType value = new ValueType();
				value.setValue(format.getName());
				outputFormat.getAllowedValues().getValueOrRange().add(value);
			}
		}

		// GetPropertyValue operation
		if (wfsConfig.getOperations().getGetPropertyValue().isEnabled()) {
			Operation getPropertyValue = new Operation();
			getPropertyValue.setName(KVPConstants.GET_PROPERTY_VALUE);		
			getPropertyValue.getDCP().add(getAndPost);
			operationsMetadata.getOperation().add(getPropertyValue);		

			DomainType outputFormat = new DomainType();
			getPropertyValue.getParameter().add(outputFormat);
			outputFormat.setName("outputFormat");
			outputFormat.setAllowedValues(new AllowedValues());
			for (OutputFormat format : wfsConfig.getOperations().getGetPropertyValue().getOutputFormats()) {
				ValueType value = new ValueType();
				value.setValue(format.getName());
				outputFormat.getAllowedValues().getValueOrRange().add(value);
			}
		}

		// GetFeature operation
		{
			Operation getFeature = new Operation();
			getFeature.setName(KVPConstants.GET_FEATURE);		
			getFeature.getDCP().add(getAndPost);
			operationsMetadata.getOperation().add(getFeature);		

			DomainType outputFormat = new DomainType();
			getFeature.getParameter().add(outputFormat);
			outputFormat.setName("outputFormat");
			outputFormat.setAllowedValues(new AllowedValues());
			for (OutputFormat format : wfsConfig.getOperations().getGetFeature().getOutputFormats()) {
				ValueType value = new ValueType();
				value.setValue(format.getName());
				outputFormat.getAllowedValues().getValueOrRange().add(value);
			}
		}

		// ListStoredQueries operation
		{
			Operation listStoredQueries = new Operation();
			listStoredQueries.setName(KVPConstants.LIST_STORED_QUERIES);		
			listStoredQueries.getDCP().add(getAndPost);
			operationsMetadata.getOperation().add(listStoredQueries);
		}

		// DescribeStoredQueries operation
		{
			Operation describeStoredQueries = new Operation();
			describeStoredQueries.setName(KVPConstants.DESCRIBE_STORED_QUERIES);		
			describeStoredQueries.getDCP().add(getAndPost);
			operationsMetadata.getOperation().add(describeStoredQueries);
		}

		// manage stored query operations
		if (wfsConfig.getOperations().getManagedStoredQueries().isEnabled()) {
			Operation dropStoredQueries = new Operation();
			dropStoredQueries.setName(KVPConstants.DROP_STORED_QUERY);		
			dropStoredQueries.getDCP().add(getAndPost);
			operationsMetadata.getOperation().add(dropStoredQueries);

			if (conformance.implementsXMLEncoding()) {
				Operation createStoredQueries = new Operation();
				createStoredQueries.setName(KVPConstants.CREATE_STORED_QUERY);		
				createStoredQueries.getDCP().add(post);
				operationsMetadata.getOperation().add(createStoredQueries);
			}
		}
	}

	private void addParameterDomainsAndConstraints(OperationsMetadata operationsMetadata) {
		// parameter domains
		// version parameter
		DomainType operationVersion = new DomainType();
		operationVersion.setName("version");
		operationVersion.setAllowedValues(new AllowedValues());

		for (String version : wfsConfig.getCapabilities().getSupportedWFSVersions()) {
			ValueType value = new ValueType();
			value.setValue(version);
			operationVersion.getAllowedValues().getValueOrRange().add(value);
		}

		operationsMetadata.getParameter().add(operationVersion);

		// resolve parameter
		DomainType resolveParameter = new DomainType();
		resolveParameter.setName("resolve");
		resolveParameter.setAllowedValues(new AllowedValues());
		ValueType noneValue = new ValueType();
		noneValue.setValue(ResolveValueType.NONE.value());
		ValueType localValue = new ValueType();
		localValue.setValue(ResolveValueType.LOCAL.value());
		resolveParameter.getAllowedValues().getValueOrRange().add(noneValue);
		resolveParameter.getAllowedValues().getValueOrRange().add(localValue);
		operationsMetadata.getParameter().add(resolveParameter);

		// mandatory constraints
		LinkedHashMap<String, ValueType> constraints = new LinkedHashMap<>();
		constraints.put("ImplementsBasicWFS", conformance.implementsBasicWFS() ? TRUE : FALSE);
		constraints.put("ImplementsTransactionalWFS", conformance.implementsTransactionalWFS() ? TRUE : FALSE);
		constraints.put("ImplementsLockingWFS", conformance.implementsLockingWFS() ? TRUE : FALSE);
		constraints.put("KVPEncoding", conformance.implementsKVPEncoding() ? TRUE : FALSE);
		constraints.put("XMLEncoding", conformance.implementsXMLEncoding() ? TRUE : FALSE);
		constraints.put("SOAPEncoding", conformance.implementsSOAPEncoding() ? TRUE : FALSE);
		constraints.put("ImplementsInheritance", conformance.implementsInheritance() ? TRUE : FALSE);
		constraints.put("ImplementsRemoteResolve", conformance.implementsRemoteResolve() ? TRUE : FALSE);
		constraints.put("ImplementsResultPaging", conformance.implementsResultPaging() ? TRUE : FALSE);
		constraints.put("ImplementsStandardJoins", conformance.implementsStandardJoins() ? TRUE : FALSE);
		constraints.put("ImplementsSpatialJoins", conformance.implementsSpatialJoins() ? TRUE : FALSE);
		constraints.put("ImplementsTemporalJoins", conformance.implementsTemporalJoins() ? TRUE : FALSE);
		constraints.put("ImplementsFeatureVersioning", conformance.implementsFeatureVersioning() ? TRUE : FALSE);
		constraints.put("ManageStoredQueries", conformance.implementsManageStoredQueries() ? TRUE : FALSE);

		for (Entry<String, ValueType> entry : constraints.entrySet()) {
			DomainType constraint = new DomainType();
			constraint.setName(entry.getKey());
			constraint.setDefaultValue(entry.getValue());
			constraint.setNoValues(new NoValues());
			operationsMetadata.getConstraint().add(constraint);
		}

		// optional constraints
		// default count
		if (wfsConfig.getConstraints().isSetCountDefault()) {
			DomainType countDefault = new DomainType();
			countDefault.setName("CountDefault");
			ValueType countDefaultValue = new ValueType();
			countDefaultValue.setValue(String.valueOf(wfsConfig.getConstraints().getCountDefault()));
			countDefault.setDefaultValue(countDefaultValue);
			countDefault.setNoValues(new NoValues());
			operationsMetadata.getConstraint().add(countDefault);
		}

		// result paging constraints
		if (conformance.implementsResultPaging()) {
			DomainType responseCacheTimeout = new DomainType();
			responseCacheTimeout.setName("ResponseCacheTimeout");
			ValueType responseCacheTimeoutValue = new ValueType();
			responseCacheTimeoutValue.setValue(String.valueOf(wfsConfig.getServer().getResponseCacheTimeout()));
			responseCacheTimeout.setDefaultValue(responseCacheTimeoutValue);
			responseCacheTimeout.setNoValues(new NoValues());
			operationsMetadata.getConstraint().add(responseCacheTimeout);

			DomainType pagingIsTransactionSafe = new DomainType();
			pagingIsTransactionSafe.setName("PagingIsTransactionSafe");
			pagingIsTransactionSafe.setDefaultValue(FALSE);
			pagingIsTransactionSafe.setNoValues(new NoValues());
			operationsMetadata.getConstraint().add(pagingIsTransactionSafe);
		}

		// announce supported query types
		DomainType queryExpressions = new DomainType();
		queryExpressions.setName("QueryExpressions");
		queryExpressions.setAllowedValues(new AllowedValues());

		if (conformance.implementsAdHocQuery()) {
			ValueType queryValue = new ValueType();
			queryValue.setValue(Constants.WFS_NAMESPACE_PREFIX + ":" + "Query");
			queryExpressions.getAllowedValues().getValueOrRange().add(queryValue);
		}

		ValueType storedQueryValue = new ValueType();
		storedQueryValue.setValue(Constants.WFS_NAMESPACE_PREFIX + ":" + "StoredQuery");
		queryExpressions.getAllowedValues().getValueOrRange().add(storedQueryValue);
		operationsMetadata.getConstraint().add(queryExpressions);
	}

	private void addFeatureTypes(FeatureTypeListType featureTypeList) {
		// create list of supported SRS
		List<DatabaseSrs> srsList = new ArrayList<>();
		srsList.add(databaseAdapter.getConnectionMetaData().getReferenceSystem());
		for (DatabaseSrs srs: wfsConfig.getDatabase().getReferenceSystems()) {
			if (srs.isSupported())
				srsList.add(srs);
		}

		// add advertised CityGML and ADE feature types
		for (FeatureType featureType : wfsConfig.getFeatureTypes().getAdvertisedFeatureTypes()) {
			FeatureTypeType type = new FeatureTypeType();
			type.setName(featureType.getName());

			for (DatabaseSrs srs : srsList) {
				String srsName = srs.getGMLSrsName();
				if (srsName == null || srsName.trim().length() == 0)
					srsName = "http://www.opengis.net/def/crs/epsg/0/" + srs.getSrid();

				if (srs == databaseAdapter.getConnectionMetaData().getReferenceSystem())
					type.setDefaultCRS(srsName);
				else
					type.getOtherCRS().add(srsName);
			}

			if (featureType.isSetTitles())
				type.getTitle().addAll(featureType.getTitles());

			if (featureType.isSetAbstracts())
				type.getAbstract().addAll(featureType.getAbstracts());

			if (featureType.isSetKeywords())
				type.getKeywords().addAll(featureType.getKeywords());

			if (featureType.isSetWGS84BoundingBoxes())
				type.getWGS84BoundingBox().addAll(featureType.getWGS84BoundingBoxes());

			if (featureType.isSetMetadataURLs())
				type.getMetadataURL().addAll(featureType.getMetadataURLs());

			if (featureType.isSetExtendedDescription())
				type.setExtendedDescription(featureType.getExtendedDescription());			

			featureTypeList.getFeatureType().add(type);
		}
	}

	private void addFilterCapabilities(Filter_Capabilities filterCapabilities) {		
		// conformance section
		ConformanceType conformanceType = new ConformanceType();
		filterCapabilities.setConformance(conformanceType);

		LinkedHashMap<String, ValueType> constraints = new LinkedHashMap<>();
		constraints.put("ImplementsQuery", conformance.implementsQuery() ? TRUE : FALSE);
		constraints.put("ImplementsAdHocQuery", conformance.implementsAdHocQuery() ? TRUE : FALSE);
		constraints.put("ImplementsFunctions", conformance.implementsFunctions() ? TRUE : FALSE);
		constraints.put("ImplementsResourceld", conformance.implementsResourceld() ? TRUE : FALSE);
		constraints.put("ImplementsMinStandardFilter", conformance.implementsMinStandardFilter() ? TRUE : FALSE);
		constraints.put("ImplementsStandardFilter", conformance.implementsStandardFilter() ? TRUE : FALSE);
		constraints.put("ImplementsMinSpatialFilter", conformance.implementsMinSpatialFilter() ? TRUE : FALSE);
		constraints.put("ImplementsSpatialFilter", conformance.implementsSpatialFilter() ? TRUE : FALSE);
		constraints.put("ImplementsMinTemporalFilter", conformance.implementsMinTemporalFilter() ? TRUE : FALSE);
		constraints.put("ImplementsTemporalFilter", conformance.implementsTemporalFilter() ? TRUE : FALSE);
		constraints.put("ImplementsVersionNav", conformance.implementsVersionNav() ? TRUE : FALSE);
		constraints.put("ImplementsSorting", conformance.implementsSorting() ? TRUE : FALSE);
		constraints.put("ImplementsExtendedOperators", conformance.implementsExtendedOperators() ? TRUE : FALSE);
		constraints.put("ImplementsMinimumXPath", conformance.implementsMinimumXPath() ? TRUE : FALSE);
		constraints.put("ImplementsSchemaElementFunc", conformance.implementsSchemaElementFunc() ? TRUE : FALSE);

		for (Entry<String, ValueType> entry : constraints.entrySet()) {
			DomainType constraint = new DomainType();
			constraint.setName(entry.getKey());
			constraint.setDefaultValue(entry.getValue());
			constraint.setNoValues(new NoValues());
			conformanceType.getConstraint().add(constraint);
		}

		// id capabilities
		Id_CapabilitiesType idCapabilities = new Id_CapabilitiesType();
		filterCapabilities.setId_Capabilities(idCapabilities);

		ResourceIdentifierType resourceIdentifier = new ResourceIdentifierType();
		resourceIdentifier.setName(new QName(Constants.FES_NAMESPACE_URI, "ResourceId"));
		idCapabilities.getResourceIdentifier().add(resourceIdentifier);

		// scalar capabilities
		if (wfsConfig.getFilterCapabilities().isSetScalarCapabilities()) {
			Scalar_CapabilitiesType scalarCapabilities = new Scalar_CapabilitiesType();
			filterCapabilities.setScalar_Capabilities(scalarCapabilities);

			if (wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetLogicalOperators()) {
				LogicalOperators logicalOperators = new LogicalOperators();
				scalarCapabilities.setLogicalOperators(logicalOperators);
			}

			if (wfsConfig.getFilterCapabilities().getScalarCapabilities().isSetComparisonOperators()) {
				ComparisonOperatorsType comparisonOperators = new ComparisonOperatorsType();
				scalarCapabilities.setComparisonOperators(comparisonOperators);

				for (ComparisonOperatorName operator : wfsConfig.getFilterCapabilities().getScalarCapabilities().getComparisonOperators()) {
					ComparisonOperatorType comparisonOperator = new ComparisonOperatorType();
					comparisonOperator.setName(operator.toString());
					comparisonOperators.getComparisonOperator().add(comparisonOperator);
				}
			}
		}

		// spatial capabilities
		if (wfsConfig.getFilterCapabilities().isSetSpatialCapabilities()) {
			Spatial_CapabilitiesType spatialCapabilities = new Spatial_CapabilitiesType();
			filterCapabilities.setSpatial_Capabilities(spatialCapabilities);

			GeometryOperandsType geometryOperands = new GeometryOperandsType();
			spatialCapabilities.setGeometryOperands(geometryOperands);

			for (SpatialOperandName operand : SpatialOperandName.values()) {
				GeometryOperand geometryOperand = new GeometryOperand();
				geometryOperand.setName(new QName(GMLCoreModule.v3_1_1.getNamespaceURI(), operand.getName()));
				geometryOperands.getGeometryOperand().add(geometryOperand);
			}

			SpatialOperatorsType spatialOperators = new SpatialOperatorsType();
			spatialCapabilities.setSpatialOperators(spatialOperators);

			for (SpatialOperatorName operator : wfsConfig.getFilterCapabilities().getSpatialCapabilities().getSpatialOperators()) {
				SpatialOperatorType spatialOperator = new SpatialOperatorType();
				spatialOperator.setName(operator.toString());
				spatialOperators.getSpatialOperator().add(spatialOperator);				
			}
		}
	}

	private void printCapabilitiesDocument(HttpServletRequest request, HttpServletResponse response, WFS_CapabilitiesType capabilities) throws WFSException {
		final SAXWriter saxWriter = new SAXWriter();

		try {
			JAXBElement<WFS_CapabilitiesType> responseElement = wfsFactory.createWFS_Capabilities(capabilities);

			// write response
			response.setContentType("text/xml");
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());

			saxWriter.setWriteEncoding(true);
			saxWriter.setIndentString("  ");
			saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			saxWriter.setPrefix(Constants.FES_NAMESPACE_PREFIX, Constants.FES_NAMESPACE_URI);
			saxWriter.setPrefix(Constants.OWS_NAMESPACE_PREFIX, Constants.OWS_NAMESPACE_URI);
			saxWriter.setPrefix(GMLCoreModule.v3_1_1.getNamespacePrefix(), GMLCoreModule.v3_1_1.getNamespaceURI());
			saxWriter.setPrefix(XLinkModule.v3_1_1.getNamespacePrefix(), XLinkModule.v3_1_1.getNamespaceURI());

			// declare CityGML namespaces
			boolean multipleVersions = wfsConfig.getFeatureTypes().getVersions().size() > 1;
			for (Module module : wfsConfig.getFeatureTypes().getModules()) {
				String prefix = module.getNamespacePrefix();
				String uri = module.getNamespaceURI();

				if (multipleVersions && module instanceof CityGMLModule) 
					prefix += (CityGMLVersion.fromCityGMLModule((CityGMLModule)module) == CityGMLVersion.v2_0_0) ? "2" : "1";

				saxWriter.setPrefix(prefix, uri);
			}

			saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);
			saxWriter.setOutput(response.getWriter());

			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
				public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
					return saxWriter.getPrefix(namespaceUri);
				}
			});

			marshaller.marshal(responseElement, saxWriter);
		} catch (JAXBException | IOException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal error occurred whilst marshalling the response document.", e);
		}
	}

}
