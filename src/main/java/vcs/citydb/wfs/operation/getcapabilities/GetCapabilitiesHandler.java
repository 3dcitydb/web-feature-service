package vcs.citydb.wfs.operation.getcapabilities;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import net.opengis.fes._2.ConformanceType;
import net.opengis.fes._2.Filter_Capabilities;
import net.opengis.fes._2.Id_CapabilitiesType;
import net.opengis.fes._2.ResourceIdentifierType;
import net.opengis.ows._1.AllowedValues;
import net.opengis.ows._1.DCP;
import net.opengis.ows._1.DomainType;
import net.opengis.ows._1.HTTP;
import net.opengis.ows._1.NoValues;
import net.opengis.ows._1.Operation;
import net.opengis.ows._1.OperationsMetadata;
import net.opengis.ows._1.RequestMethodType;
import net.opengis.ows._1.ValueType;
import net.opengis.wfs._2.FeatureTypeListType;
import net.opengis.wfs._2.FeatureTypeType;
import net.opengis.wfs._2.GetCapabilitiesType;
import net.opengis.wfs._2.WFS_CapabilitiesType;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.gml.GMLCoreModule;
import org.citygml4j.model.module.gml.XLinkModule;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.capabilities.OWSMetadata;
import vcs.citydb.wfs.config.conformance.Conformance;
import vcs.citydb.wfs.config.feature.FeatureType;
import vcs.citydb.wfs.config.operation.OutputFormat;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.util.LoggerUtil;

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

	public GetCapabilitiesHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) throws JAXBException {
		this.wfsConfig = wfsConfig;

		conformance = new Conformance(wfsConfig);
		wfsFactory = new net.opengis.wfs._2.ObjectFactory();
		owsFactory = new net.opengis.ows._1.ObjectFactory();
		marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
	}

	public void doOperation(GetCapabilitiesType wfsRequest,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {
		log.info(LoggerUtil.getLogMessage(request, "Accepting GetCapabilities request."));

		// check service attribute
		if (!wfsRequest.isSetService() || !Constants.WFS_SERVICE_STRING.equals(wfsRequest.getService()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'service' must match the fixed value '" + Constants.WFS_SERVICE_STRING + "'.");		

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
		addOperations(operations);

		// add service and operation constraints
		addServiceAndOperationConstraints(operations);

		// add feature types
		addFeatureTypes(featureTypeList);

		// add filter capabilities
		addFilterCapabilities(filterCapabilities);

		// write response
		printCapabilitiesDocument(request, response, capabilities);

		log.info(LoggerUtil.getLogMessage(request, "GetCapabilities operation successfully finished."));
	}

	private void addOperations(OperationsMetadata operationsMetadata) {
		// add operations to satisfy WFS Simple conformance class
		// create external service URL
		String externalServiceURL = wfsConfig.getServer().getExternalServiceURL();
		if (externalServiceURL.endsWith("/"))
			externalServiceURL = externalServiceURL.substring(0, externalServiceURL.length() - 1);

		externalServiceURL += Constants.WFS_SERVICE_PATH;
		RequestMethodType request = new RequestMethodType();
		request.setHref(externalServiceURL);

		DCP getAndPost = new DCP();
		getAndPost.setHTTP(new HTTP());
		if (conformance.implementsKVPEncoding())
			getAndPost.getHTTP().getGetOrPost().add(owsFactory.createHTTPGet(request));
		if (conformance.implementsXMLEncoding())
			getAndPost.getHTTP().getGetOrPost().add(owsFactory.createHTTPPost(request));

		DCP post = new DCP();
		post.setHTTP(new HTTP());
		post.getHTTP().getGetOrPost().add(owsFactory.createHTTPPost(request));

		// operations version
		DomainType operationVersion = new DomainType();
		operationVersion.setName(KVPConstants.VERSION);
		operationVersion.setAllowedValues(new AllowedValues());

		for (String version : wfsConfig.getCapabilities().getSupportedWFSVersions()) {
			ValueType value = new ValueType();
			value.setValue(version);
			operationVersion.getAllowedValues().getValueOrRange().add(value);
		}

		// GetCapabilities operation
		{
			Operation getCapabilities = new Operation();
			getCapabilities.setName(KVPConstants.GET_CAPABILITIES);		
			getCapabilities.getDCP().add(getAndPost);		
			operationsMetadata.getOperation().add(getCapabilities);

			DomainType acceptVersions = new DomainType();
			getCapabilities.getParameter().add(acceptVersions);
			acceptVersions.setName(KVPConstants.ACCEPT_VERSIONS);
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

		operationsMetadata.getParameter().add(operationVersion);
	}

	private void addServiceAndOperationConstraints(OperationsMetadata operationsMetadata) {
		ValueType trueValue = new ValueType();
		trueValue.setValue("TRUE");

		ValueType falseValue = new ValueType();
		falseValue.setValue("FALSE");

		// mandatory constraints
		LinkedHashMap<String, ValueType> constraints = new LinkedHashMap<>();
		constraints.put("ImplementsBasicWFS", conformance.implementsBasicWFS() ? trueValue : falseValue);
		constraints.put("ImplementsTransactionalWFS", conformance.implementsTransactionalWFS() ? trueValue : falseValue);
		constraints.put("ImplementsLockingWFS", conformance.implementsLockingWFS() ? trueValue : falseValue);
		constraints.put("KVPEncoding", conformance.implementsKVPEncoding() ? trueValue : falseValue);
		constraints.put("XMLEncoding", conformance.implementsXMLEncoding() ? trueValue : falseValue);
		constraints.put("SOAPEncoding", conformance.implementsSOAPEncoding() ? trueValue : falseValue);
		constraints.put("ImplementsInheritance", conformance.implementsInheritance() ? trueValue : falseValue);
		constraints.put("ImplementsRemoteResolve", conformance.implementsRemoteResolve() ? trueValue : falseValue);
		constraints.put("ImplementsResultPaging", conformance.implementsResultPaging() ? trueValue : falseValue);
		constraints.put("ImplementsStandardJoins", conformance.implementsStandardJoins() ? trueValue : falseValue);
		constraints.put("ImplementsSpatialJoins", conformance.implementsSpatialJoins() ? trueValue : falseValue);
		constraints.put("ImplementsTemporalJoins", conformance.implementsTemporalJoins() ? trueValue : falseValue);
		constraints.put("ImplementsFeatureVersioning", conformance.implementsFeatureVersioning() ? trueValue : falseValue);
		constraints.put("ManageStoredQueries", conformance.implementsManageStoredQueries() ? trueValue : falseValue);			

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

		// announce supported query types
		DomainType queryExpressions = new DomainType();
		queryExpressions.setName("QueryExpressions");
		queryExpressions.setAllowedValues(new AllowedValues());

		ValueType storedQueryValue = new ValueType();
		storedQueryValue.setValue(new StringBuilder(Constants.WFS_NAMESPACE_PREFIX).append(":").append("StoredQuery").toString());
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

		ValueType trueValue = new ValueType();
		trueValue.setValue("TRUE");

		ValueType falseValue = new ValueType();
		falseValue.setValue("FALSE");

		LinkedHashMap<String, ValueType> constraints = new LinkedHashMap<>();
		constraints.put("ImplementsQuery", conformance.implementsQuery() ? trueValue : falseValue);
		constraints.put("ImplementsAdHocQuery", conformance.implementsAdHocQuery() ? trueValue : falseValue);
		constraints.put("ImplementsFunctions", conformance.implementsFunctions() ? trueValue : falseValue);
		constraints.put("ImplementsResourceld", conformance.implementsResourceld() ? trueValue : falseValue);
		constraints.put("ImplementsMinStandardFilter", conformance.implementsMinStandardFilter() ? trueValue : falseValue);		
		constraints.put("ImplementsStandardFilter", conformance.implementsStandardFilter() ? trueValue : falseValue);
		constraints.put("ImplementsMinSpatialFilter", conformance.implementsMinSpatialFilter() ? trueValue : falseValue);
		constraints.put("ImplementsSpatialFilter", conformance.implementsSpatialFilter() ? trueValue : falseValue);
		constraints.put("ImplementsMinTemporalFilter", conformance.implementsMinTemporalFilter() ? trueValue : falseValue);
		constraints.put("ImplementsTemporalFilter", conformance.implementsTemporalFilter() ? trueValue : falseValue);
		constraints.put("ImplementsVersionNav", conformance.implementsVersionNav() ? trueValue : falseValue);
		constraints.put("ImplementsSorting", conformance.implementsSorting() ? trueValue : falseValue);
		constraints.put("ImplementsExtendedOperators", conformance.implementsExtendedOperators() ? trueValue : falseValue);
		constraints.put("ImplementsMinimumXPath", conformance.implementsMinimumXPath() ? trueValue : falseValue);
		constraints.put("ImplementsSchemaElementFunc", conformance.implementsSchemaElementFunc() ? trueValue : falseValue);

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
			saxWriter.setOutput(response.getOutputStream(), StandardCharsets.UTF_8.name());

			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
				public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
					return saxWriter.getPrefix(namespaceUri);
				}
			});

			marshaller.marshal(responseElement, saxWriter);

			// flush SAX writer
			saxWriter.flush();
		} catch (JAXBException | IOException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal error occurred whilst marshalling the response document.", e);
		} catch (SAXException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close the SAX writer.", e);			
		}
	}

}
