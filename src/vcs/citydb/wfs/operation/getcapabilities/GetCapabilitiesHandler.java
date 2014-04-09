/*
 * This file is part of the 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright (c) 2014
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 * 
 * The 3D City Database Web Feature Service is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package vcs.citydb.wfs.operation.getcapabilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

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

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.gml.GMLCoreModule;
import org.citygml4j.model.module.gml.XLinkModule;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.capabilities.OWSMetadata;
import vcs.citydb.wfs.config.feature.FeatureType;
import vcs.citydb.wfs.config.operation.DescribeFeatureTypeOutputFormat;
import vcs.citydb.wfs.config.operation.GetFeatureOutputFormat;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.util.LoggerUtil;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;

public class GetCapabilitiesHandler {
	private final Logger log = Logger.getInstance();
	private final WFSConfig wfsConfig;

	private final Marshaller marshaller;
	private final net.opengis.wfs._2.ObjectFactory wfsFactory;
	private final net.opengis.ows._1.ObjectFactory owsFactory;
	private final DatabaseConnectionPool connectionPool;

	public GetCapabilitiesHandler(JAXBBuilder jaxbBuilder, WFSConfig wfsConfig) throws JAXBException {
		this.wfsConfig = wfsConfig;

		wfsFactory = new net.opengis.wfs._2.ObjectFactory();
		owsFactory = new net.opengis.ows._1.ObjectFactory();
		marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
		connectionPool = DatabaseConnectionPool.getInstance();
	}

	public void doOperation(GetCapabilitiesType wfsRequest,
			ServletContext servletContext,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {
		log.info(LoggerUtil.getLogMessage(request, "Accepting GetCapabilities request."));


		// check service attribute
		if (!wfsRequest.isSetService() || !Constants.WFS_SERVICE_STRING.equals(wfsRequest.getService()))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The attribute 'service' must match the fixed value '" + Constants.WFS_SERVICE_STRING + "'.");		

		// check whether we are supposed to return a static capabilities document
		boolean dynamic = true;
		if (wfsConfig.getCapabilities() != null && wfsConfig.getCapabilities().getValue() instanceof String) {
			File capabilities = new File((String)wfsConfig.getCapabilities().getValue());
			if (capabilities.exists() && capabilities.canRead()) {
				printStaticCapabilitiesDocument(request, response, capabilities);
				dynamic = false;
			}
		}		

		if (dynamic) {
			// dynamically generate capabilities document
			WFS_CapabilitiesType capabilities = new WFS_CapabilitiesType();
			capabilities.setVersion(Constants.WFS_VERSION_STRING);

			// service identification and provider are copied from configuration file
			if (wfsConfig.getCapabilities() != null && wfsConfig.getCapabilities().getValue() instanceof OWSMetadata) {
				OWSMetadata owsMetadata = (OWSMetadata)wfsConfig.getCapabilities().getValue();
				capabilities.setServiceIdentification(owsMetadata.getServiceIdentification());
				capabilities.setServiceProvider(owsMetadata.getServiceProvider());
			}

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
			printDynamicCapabilitiesDocument(request, response, capabilities);
		}

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
		getAndPost.getHTTP().getGetOrPost().add(owsFactory.createHTTPGet(request));
		getAndPost.getHTTP().getGetOrPost().add(owsFactory.createHTTPPost(request));

		DCP post = new DCP();
		post.setHTTP(new HTTP());
		post.getHTTP().getGetOrPost().add(owsFactory.createHTTPPost(request));

		// operations version
		DomainType operationVersion = new DomainType();
		operationVersion.setName("version");
		operationVersion.setAllowedValues(new AllowedValues());
		ValueType operationVersionValue = new ValueType();
		operationVersionValue.setValue(Constants.WFS_VERSION_STRING);
		operationVersion.getAllowedValues().getValueOrRange().add(operationVersionValue);

		// GetCapabilities operation
		{
			Operation getCapabilities = new Operation();
			getCapabilities.setName("GetCapabilities");		
			getCapabilities.getDCP().add(getAndPost);		
			operationsMetadata.getOperation().add(getCapabilities);

			DomainType acceptVersions = new DomainType();
			getCapabilities.getParameter().add(acceptVersions);
			acceptVersions.setName("AcceptVersions");
			acceptVersions.setAllowedValues(new AllowedValues());
			ValueType version = new ValueType();
			version.setValue(Constants.WFS_VERSION_STRING);
			acceptVersions.getAllowedValues().getValueOrRange().add(version);

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
			describeFeatureType.setName("DescribeFeatureType");		
			describeFeatureType.getDCP().add(post);
			operationsMetadata.getOperation().add(describeFeatureType);		

			DomainType outputFormat = new DomainType();
			describeFeatureType.getParameter().add(outputFormat);
			outputFormat.setName("outputFormat");
			outputFormat.setAllowedValues(new AllowedValues());
			for (DescribeFeatureTypeOutputFormat format : wfsConfig.getOperations().getDescribeFeatureType().getOutputFormat()) {
				ValueType value = new ValueType();
				value.setValue(format.value());
				outputFormat.getAllowedValues().getValueOrRange().add(value);
			}
		}

		// GetFeature operation
		{
			Operation getFeature = new Operation();
			getFeature.setName("GetFeature");		
			getFeature.getDCP().add(post);
			operationsMetadata.getOperation().add(getFeature);		

			DomainType outputFormat = new DomainType();
			getFeature.getParameter().add(outputFormat);
			outputFormat.setName("outputFormat");
			outputFormat.setAllowedValues(new AllowedValues());
			for (GetFeatureOutputFormat format : wfsConfig.getOperations().getGetFeature().getOutputFormat()) {
				ValueType value = new ValueType();
				value.setValue(format.value());
				outputFormat.getAllowedValues().getValueOrRange().add(value);
			}
		}

		// ListStoredQueries operation
		{
			Operation listStoredQueries = new Operation();
			listStoredQueries.setName("ListStoredQueries");		
			listStoredQueries.getDCP().add(post);
			operationsMetadata.getOperation().add(listStoredQueries);
		}

		// DescribeStoredQueries operation
		{
			Operation describeStoredQueries = new Operation();
			describeStoredQueries.setName("DescribeStoredQueries");		
			describeStoredQueries.getDCP().add(post);
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
		constraints.put("ImplementsBasicWFS", falseValue);
		constraints.put("ImplementsTransactionalWFS", falseValue);
		constraints.put("ImplementsLockingWFS", falseValue);
		constraints.put("KVPEncoding", falseValue);
		constraints.put("XMLEncoding", trueValue);
		constraints.put("SOAPEncoding", falseValue);
		constraints.put("ImplementsInheritance", falseValue);
		constraints.put("ImplementsRemoteResolve", falseValue);
		constraints.put("ImplementsResultPaging", falseValue);
		constraints.put("ImplementsStandardJoins", falseValue);
		constraints.put("ImplementsSpatialJoins", falseValue);
		constraints.put("ImplementsTemporalJoins", falseValue);
		constraints.put("ImplementsFeatureVersioning", falseValue);
		constraints.put("ManageStoredQueries", falseValue);			

		for (Entry<String, ValueType> entry : constraints.entrySet()) {
			DomainType constraint = new DomainType();
			constraint.setName(entry.getKey());
			constraint.setDefaultValue(entry.getValue());
			constraint.setNoValues(new NoValues());
			operationsMetadata.getConstraint().add(constraint);
		}

		// optional constraints
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
		for (FeatureType featureType : wfsConfig.getFeatureTypes().getFeatureTypes()) {
			for (CityGMLVersion version : wfsConfig.getFeatureTypes().getVersions()) {
				// announce feature type by name
				FeatureTypeType type = new FeatureTypeType();
				QName name = featureType.getQName(version);
				if (name == null)
					continue;

				type.setName(name);
				featureTypeList.getFeatureType().add(type);

				// create list of supported SRS
				List<DatabaseSrs> srsList = new ArrayList<>();
				srsList.add(connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem());
				for (DatabaseSrs srs: wfsConfig.getDatabase().getReferenceSystems())
					if (srs.isSupported())
						srsList.add(srs);

				for (DatabaseSrs srs : srsList) {
					String srsName = srs.getGMLSrsName();
					if (srsName == null || srsName.trim().length() == 0)
						srsName = "urn:ogc:def:crs:EPSG::" + srs.getSrid();

					if (srs == connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem())
						type.setDefaultCRS(srsName);
					else
						type.getOtherCRS().add(srsName);
				}

				// announce bounding box
				if (featureType.getWGS84BoundingBox() != null)
					type.getWGS84BoundingBox().add(featureType.getWGS84BoundingBox());
			}
		}
	}

	private void addFilterCapabilities(Filter_Capabilities filterCapabilities) {
		// conformance section
		ConformanceType conformance = new ConformanceType();
		filterCapabilities.setConformance(conformance);

		ValueType trueValue = new ValueType();
		trueValue.setValue("TRUE");

		ValueType falseValue = new ValueType();
		falseValue.setValue("FALSE");

		LinkedHashMap<String, ValueType> constraints = new LinkedHashMap<>();
		constraints.put("ImplementsQuery", trueValue);
		constraints.put("ImplementsAdHocQuery", falseValue);
		constraints.put("ImplementsFunctions", falseValue);
		constraints.put("ImplementsMinStandardFilter", falseValue);
		constraints.put("ImplementsStandardFilter", falseValue);
		constraints.put("ImplementsMinSpatialFilter", falseValue);
		constraints.put("ImplementsSpatialFilter", falseValue);
		constraints.put("ImplementsMinTemporalFilter", falseValue);
		constraints.put("ImplementsTemporalFilter", falseValue);
		constraints.put("ImplementsVersionNav", falseValue);
		constraints.put("ImplementsSorting", falseValue);
		constraints.put("ImplementsExtendedOperators", falseValue);

		for (Entry<String, ValueType> entry : constraints.entrySet()) {
			DomainType constraint = new DomainType();
			constraint.setName(entry.getKey());
			constraint.setDefaultValue(entry.getValue());
			constraint.setNoValues(new NoValues());
			conformance.getConstraint().add(constraint);
		}

		// id capabilities
		Id_CapabilitiesType idCapabilities = new Id_CapabilitiesType();
		filterCapabilities.setId_Capabilities(idCapabilities);

		ResourceIdentifierType resourceIdentifier = new ResourceIdentifierType();
		resourceIdentifier.setName(new QName(Constants.FES_NAMESPACE_URI, "ResourceId"));
		idCapabilities.getResourceIdentifier().add(resourceIdentifier);
	}

	private void printDynamicCapabilitiesDocument(HttpServletRequest request, HttpServletResponse response, WFS_CapabilitiesType capabilities) throws WFSException {
		final SAXWriter saxWriter = new SAXWriter();

		try {
			JAXBElement<WFS_CapabilitiesType> responseElement = wfsFactory.createWFS_Capabilities(capabilities);

			// write response
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");

			saxWriter.setWriteEncoding(true);
			saxWriter.setIndentString("  ");
			saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			saxWriter.setPrefix(Constants.FES_NAMESPACE_PREFIX, Constants.FES_NAMESPACE_URI);
			saxWriter.setPrefix(Constants.OWS_NAMESPACE_PREFIX, Constants.OWS_NAMESPACE_URI);
			saxWriter.setPrefix(GMLCoreModule.v3_1_1.getNamespacePrefix(), GMLCoreModule.v3_1_1.getNamespaceURI());
			saxWriter.setPrefix(XLinkModule.v3_1_1.getNamespacePrefix(), XLinkModule.v3_1_1.getNamespaceURI());

			// declare CityGML namespaces
			boolean multipleVersions = wfsConfig.getFeatureTypes().getVersions().size() > 1;
			for (CityGMLModule module : wfsConfig.getFeatureTypes().getCityGMLModules()) {
				String prefix = module.getNamespacePrefix();
				String uri = module.getNamespaceURI();

				if (multipleVersions) 
					prefix += (CityGMLVersion.fromCityGMLModule(module) == CityGMLVersion.v2_0_0) ? "2" : "1";

				saxWriter.setPrefix(prefix, uri);
			}

			saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);
			saxWriter.setOutput(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));

			marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
				public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
					return saxWriter.getPrefix(namespaceUri);
				}
			});

			marshaller.marshal(responseElement, saxWriter);

			// close SAX writer. this also closes the servlet output stream.
			saxWriter.close();
		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal JAXB error occurred whilst marshalling the response document.", e);
		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal SAX error occurred whilst marshalling the response document.", e);
		} catch (SAXException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close the SAX writer..", e);			
		}
	}

	private void printStaticCapabilitiesDocument(HttpServletRequest request, HttpServletResponse response, File capabilities) throws WFSException {
		try {
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");

			BufferedReader reader = new BufferedReader(new FileReader(capabilities));
			PrintWriter writer = response.getWriter();
			String line;

			while ((line = reader.readLine()) != null)
				writer.println(line);

			writer.flush();
			reader.close();
		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to read local capabilities document.", e);
		}
	}
}
