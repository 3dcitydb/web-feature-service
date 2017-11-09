/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vcs.citydb.wfs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.opengis.ows._1.AcceptVersionsType;
import net.opengis.wfs._2.DescribeFeatureTypeType;
import net.opengis.wfs._2.DescribeStoredQueriesType;
import net.opengis.wfs._2.GetCapabilitiesType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ListStoredQueriesType;

import org.citydb.api.concurrent.SingleWorkerPool;
import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerFactory;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.xml.schema.SchemaHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionReportHandler;
import vcs.citydb.wfs.operation.describefeaturetype.DescribeFeatureTypeHandler;
import vcs.citydb.wfs.operation.getcapabilities.GetCapabilitiesHandler;
import vcs.citydb.wfs.operation.getfeature.GetFeatureHandler;
import vcs.citydb.wfs.operation.storedquery.DescribeStoredQueriesHandler;
import vcs.citydb.wfs.operation.storedquery.ListStoredQueriesHandler;
import vcs.citydb.wfs.operation.storedquery.StoredQueryManager;
import vcs.citydb.wfs.util.CacheTableCleanerWorker;
import vcs.citydb.wfs.xml.NamespaceFilter;
import vcs.citydb.wfs.xml.ValidationEventHandlerImpl;

@WebServlet(Constants.WFS_SERVICE_PATH)
public class WFSService extends HttpServlet { 
	private static final long serialVersionUID = 1L;
	private final Logger log = Logger.getInstance();

	private WFSConfig wfsConfig;
	private Config exporterConfig;
	private JAXBBuilder jaxbBuilder;
	private SAXParserFactory saxParserFactory;
	private Schema wfsSchema;
	private ArrayBlockingQueue<String> requestQueue;
	private SingleWorkerPool<CacheTableManager> cacheTableCleanerPool;
	private WFSExceptionReportHandler exceptionReportHandler;

	@Override
	public void init() throws ServletException {
		// check whether servlet initialization threw an error
		Object error = getServletContext().getAttribute(Constants.INIT_ERROR_ATTRNAME);
		if (error instanceof ServletException)
			throw (ServletException)error;

		log.info("WFS service is loaded by servlet container.");

		// service specific initialization
		ObjectRegistry registry = ObjectRegistry.getInstance();
		jaxbBuilder = (JAXBBuilder)registry.lookup(JAXBBuilder.class.getName());
		wfsConfig = (WFSConfig)registry.lookup(WFSConfig.class.getName());
		exporterConfig = (Config)registry.lookup(Config.class.getName());
		requestQueue = new ArrayBlockingQueue<String>(wfsConfig.getServer().getMaxParallelRequests(), true);
		exceptionReportHandler = new WFSExceptionReportHandler(jaxbBuilder);

		saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);

		try {
			StoredQueryManager storedQueryManager = new StoredQueryManager(jaxbBuilder, wfsConfig);
			registry.register(StoredQueryManager.class.getName(), storedQueryManager);
		} catch (ParserConfigurationException e) {
			String message = "Failed to initialize stored query manager.";
			log.error(message);
			log.error(e.getMessage());
			throw new ServletException(message, e);
		} catch (SAXException e) {
			String message = "Failed to initialize stored query manager.";
			log.error(message);
			log.error(e.getMessage());
			throw new ServletException(message, e);
		}

		// read WFS 2.0 schema to validate requests
		if (wfsConfig.getOperations().isUseXMLValidation()) {
			try {
				SchemaHandler schemaHandler = (SchemaHandler)registry.lookup(SchemaHandler.class.getName());
				schemaHandler.parseSchema(new File(getServletContext().getRealPath(Constants.XML_SCHEMAS_PATH + "/wfs/2.0.2/wfs.xsd")));
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				wfsSchema = schemaFactory.newSchema(schemaHandler.getSchemaSources());			
			} catch (SAXException e) {
				String message = "Failed to read WFS XML Schema from " + Constants.XML_SCHEMAS_PATH + "/wfs/2.0.2/wfs.xsd.";
				log.error(message);
				log.error(e.getMessage());
				throw new ServletException(message, e);
			}
		}

		// register cache table cleaner pool
		cacheTableCleanerPool = new SingleWorkerPool<CacheTableManager>(
				"cache_table_cleaner",
				new WorkerFactory<CacheTableManager>() {
					public Worker<CacheTableManager> createWorker() {
						return new CacheTableCleanerWorker();
					}
				}, 
				1);

		cacheTableCleanerPool.prestartCoreWorker();
		registry.register(CacheTableCleanerWorker.class.getName(), cacheTableCleanerPool);
	}

	@Override
	public void destroy() {
		log.info("WFS service is destroyed by servlet container.");

		// destroy resources which may otherwise cause memory leaks
		try {
			cacheTableCleanerPool.shutdownAndWait();
		} catch (InterruptedException e) {
			String message = "Failed to clean temporay tables.";
			log.error(message);
			log.error(e.getMessage());
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> tmp = request.getParameterMap();
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();

		for (Entry<String, String[]> entry : tmp.entrySet())
			parameterMap.put(entry.getKey().toUpperCase(), entry.getValue());

		try {
			if (parameterMap.containsKey("REQUEST")) {
				if (parameterMap.get("REQUEST").length > 1)
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Multiple REQUEST keywords are not allowed.");

				String requestValue = parameterMap.get("REQUEST")[0];

				if (requestValue.equals("GetCapabilities")) {
					// translate into JAXB object
					// TODO: improve mapping
					GetCapabilitiesType wfsRequest = new GetCapabilitiesType();
					wfsRequest.setService(parameterMap.containsKey("SERVICE") ? parameterMap.get("SERVICE")[0] : null);

					if (parameterMap.containsKey("ACCEPTVERSIONS")) {
						AcceptVersionsType acceptVersions = new AcceptVersionsType();
						for (String version : parameterMap.get("ACCEPTVERSIONS")[0].split(",")) {
							if (!version.isEmpty())
								acceptVersions.getVersion().add(version.trim());
						}

						wfsRequest.setAcceptVersions(acceptVersions);
					}

					// handle GetCapabilities request
					GetCapabilitiesHandler getCapabilitiesHandler = new GetCapabilitiesHandler(jaxbBuilder, wfsConfig);
					getCapabilitiesHandler.doOperation(wfsRequest, getServletContext(), request, response);
					return;
				}	
			}

			// TODO: KVP binding over HTTP GET is not supported so far...
			throw new WFSException(WFSExceptionCode.NO_APPLICABLE_CODE, "HTTP GET is not supported by this WFS implementation.");

		} catch (JAXBException e) {
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to unmarshal the XML message.", e), request, response); 
		}catch (WFSException e) {
			exceptionReportHandler.sendErrorResponse(e, request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// set CORS http headers
			if (wfsConfig.getServer().isEnableCORS())
				response.addHeader("Access-Control-Allow-Origin", "*");

			Unmarshaller unmarshaller = jaxbBuilder.getJAXBContext().createUnmarshaller();
			ValidationEventHandlerImpl validationEventHandler = null;

			// support XML validation
			if (wfsConfig.getOperations().isUseXMLValidation()) {
				unmarshaller.setSchema(wfsSchema);
				validationEventHandler = new ValidationEventHandlerImpl();
				unmarshaller.setEventHandler(validationEventHandler);
			}

			UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();			

			// use SAX parser to keep track of namespace declarations
			SAXParser parser = saxParserFactory.newSAXParser();			
			XMLReader reader = parser.getXMLReader();
			NamespaceFilter namespaceFilter = new NamespaceFilter(reader);
			namespaceFilter.setContentHandler(unmarshallerHandler);

			try {
				namespaceFilter.parse(new InputSource(request.getInputStream()));		
			} catch (SAXException e) {
				if (validationEventHandler != null && !validationEventHandler.isValid())
					throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, validationEventHandler.getCause());
				else throw e;
			}	

			// unmarshal WFS request
			// TODO: add native support for GML 3.2
			Object object = unmarshallerHandler.getResult();
			if (!(object instanceof JAXBElement<?>))
				throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "Failed to parse XML document received through HTTP POST.");

			JAXBElement<?> jaxbElement = (JAXBElement<?>)object;
			Object wfsRequest = ((JAXBElement<?>)object).getValue();

			if (wfsRequest instanceof GetFeatureType) {
				try {
					// make sure we only serve a maximum number of requests in parallel
					if (!requestQueue.offer(request.getRemoteAddr(), wfsConfig.getServer().getWaitTimeout(), TimeUnit.SECONDS))
						throw new WFSException(WFSExceptionCode.SERVICE_UNAVAILABLE, "The service is currently unavailable because it is overloaded. " +
								"Generally, this is a temporary state. Please retry later.");

					// handle GetFeature request
					GetFeatureHandler getFeatureHandler = new GetFeatureHandler(jaxbBuilder, wfsConfig, exporterConfig);
					getFeatureHandler.doOperation((GetFeatureType)wfsRequest, namespaceFilter, request, response);
				} catch (InterruptedException e) {
					throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "The service has internally interrupted the request: " + e.getMessage());
				} finally {
					// free one slot from the request queue
					requestQueue.remove(request.getRemoteAddr());
				}
			}

			else if (wfsRequest instanceof DescribeFeatureTypeType) {
				// handle DescribeFeatureType request
				DescribeFeatureTypeHandler describeFeatureTypeHandler = new DescribeFeatureTypeHandler(wfsConfig);
				describeFeatureTypeHandler.doOperation((DescribeFeatureTypeType)wfsRequest, getServletContext(), request, response);
			}

			else if (wfsRequest instanceof GetCapabilitiesType) {				
				// handle GetCapabilities request
				GetCapabilitiesHandler getCapabilitiesHandler = new GetCapabilitiesHandler(jaxbBuilder, wfsConfig);
				getCapabilitiesHandler.doOperation((GetCapabilitiesType)wfsRequest, getServletContext(), request, response);
			}

			else if (wfsRequest instanceof ListStoredQueriesType) {
				// handle ListStoredQueries request
				ListStoredQueriesHandler listStoredQueriesHandler = new ListStoredQueriesHandler(jaxbBuilder);
				listStoredQueriesHandler.doOperation((ListStoredQueriesType)wfsRequest, request, response);
			}

			else if (wfsRequest instanceof DescribeStoredQueriesType) {
				// handle ListStoredQueries request
				DescribeStoredQueriesHandler describeStoredQueriesHandler = new DescribeStoredQueriesHandler(jaxbBuilder);
				describeStoredQueriesHandler.doOperation((DescribeStoredQueriesType)wfsRequest, request, response);
			}

			else if (wfsRequest != null)
				throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "The operation " + jaxbElement.getName().toString() + " is not supported by this WFS implementation.");

			else
				throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "Failed to parse the requested operation.");

		} catch (JAXBException e) {
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to unmarshal the XML message.", e), request, response); 
		} catch (SAXException e) {			
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "Failed to parse the XML message.", e), request, response); 
		} catch (ParserConfigurationException e) {
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize a SAX parser for parsing the XML message.", e), request, response); 
		} catch (WFSException e) {
			if (!response.isCommitted())
				response.reset();

			exceptionReportHandler.sendErrorResponse(e, request, response);
		}
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// support CORS preflight requests
		if (wfsConfig.getServer().isEnableCORS()) {
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "GET, POST");
			response.setHeader("Access-Control-Max-Age", "86400");

			String requestCORSHeaders = request.getHeader("Access-Control-Request-Headers");
			if (requestCORSHeaders != null)
				response.setHeader("Access-Control-Allow-Headers", requestCORSHeaders);
		}
	}

}
