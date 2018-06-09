package vcs.citydb.wfs;

import net.opengis.wfs._2.DescribeFeatureTypeType;
import net.opengis.wfs._2.DescribeStoredQueriesType;
import net.opengis.wfs._2.GetCapabilitiesType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ListStoredQueriesType;
import org.citydb.concurrent.SingleWorkerPool;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.config.Config;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.xml.schema.SchemaHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.EncodingMethod;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionReportHandler;
import vcs.citydb.wfs.kvp.DescribeFeatureTypeReader;
import vcs.citydb.wfs.kvp.DescribeStoredQueriesReader;
import vcs.citydb.wfs.kvp.GetCapabilitiesReader;
import vcs.citydb.wfs.kvp.GetFeatureReader;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.kvp.KVPRequestReader;
import vcs.citydb.wfs.kvp.ListStoredQueriesReader;
import vcs.citydb.wfs.operation.describefeaturetype.DescribeFeatureTypeHandler;
import vcs.citydb.wfs.operation.getcapabilities.GetCapabilitiesHandler;
import vcs.citydb.wfs.operation.getfeature.GetFeatureHandler;
import vcs.citydb.wfs.operation.storedquery.DescribeStoredQueriesHandler;
import vcs.citydb.wfs.operation.storedquery.ListStoredQueriesHandler;
import vcs.citydb.wfs.operation.storedquery.StoredQueryManager;
import vcs.citydb.wfs.util.CacheCleanerWork;
import vcs.citydb.wfs.util.CacheCleanerWorker;
import vcs.citydb.wfs.util.DatabaseConnector;
import vcs.citydb.wfs.xml.NamespaceFilter;
import vcs.citydb.wfs.xml.ValidationEventHandlerImpl;

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
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@WebServlet(Constants.WFS_SERVICE_PATH)
public class WFSService extends HttpServlet { 
	private static final long serialVersionUID = 1L;
	private final Logger log = Logger.getInstance();
	private final DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();

	private WFSConfig wfsConfig;
	private Config exporterConfig;
	private CityGMLBuilder cityGMLBuilder;
	private SAXParserFactory saxParserFactory;
	private Schema wfsSchema;
	private ArrayBlockingQueue<String> requestQueue;
	private SingleWorkerPool<CacheCleanerWork> cacheCleanerPool;
	private WFSExceptionReportHandler exceptionReportHandler;

	@Override
	public void init() throws ServletException {
		// check whether servlet initialization threw an error
		Object error = getServletContext().getAttribute(Constants.INIT_ERROR_ATTRNAME);
		if (error instanceof ServletException)
			throw (ServletException)error;

		log.info("WFS service is loaded by the servlet container.");

		// service specific initialization
		ObjectRegistry registry = ObjectRegistry.getInstance();
		cityGMLBuilder = registry.getCityGMLBuilder();
		wfsConfig = (WFSConfig)registry.lookup(WFSConfig.class.getName());
		exporterConfig = (Config)registry.lookup(Config.class.getName());
		requestQueue = new ArrayBlockingQueue<String>(wfsConfig.getServer().getMaxParallelRequests(), true);
		exceptionReportHandler = new WFSExceptionReportHandler(cityGMLBuilder);

		saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);

		try {
			StoredQueryManager storedQueryManager = new StoredQueryManager(cityGMLBuilder, wfsConfig);
			registry.register(StoredQueryManager.class.getName(), storedQueryManager);
		} catch (ParserConfigurationException | SAXException | NoSuchAlgorithmException | IOException e) {
			String message = "Failed to initialize stored query manager.";
			log.error(message);
			log.error(e.getMessage());
			throw new ServletException(message, e);
		}

		// read WFS 2.0 schema to validate requests
		if (wfsConfig.getOperations().getRequestEncoding().isUseXMLValidation()) {
			try {
				SchemaHandler schemaHandler = (SchemaHandler)registry.lookup(SchemaHandler.class.getName());
				schemaHandler.parseSchema(new File(getServletContext().getRealPath(Constants.SCHEMAS_PATH + "/ogc/wfs/2.0.2/wfs.xsd")));
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				wfsSchema = schemaFactory.newSchema(schemaHandler.getSchemaSources());
			} catch (SAXException e) {
				String message = "Failed to read WFS XML Schema from " + Constants.SCHEMAS_PATH + "/ogc/wfs.";
				log.error(message);
				log.error(e.getMessage());
				throw new ServletException(message, e);
			}
		}

		// register cache table cleaner pool
		cacheCleanerPool = new SingleWorkerPool<CacheCleanerWork>(
				"cache_cleaner",
				new WorkerFactory<CacheCleanerWork>() {
					public Worker<CacheCleanerWork> createWorker() {
						return new CacheCleanerWorker();
					}
				}, 
				wfsConfig.getServer().getMaxParallelRequests());

		cacheCleanerPool.prestartCoreWorker();
		registry.register(CacheCleanerWorker.class.getName(), cacheCleanerPool);
	}

	@Override
	public void destroy() {
		log.info("WFS service is destroyed by the servlet container.");

		// destroy resources which may otherwise cause memory leaks
		try {
			cacheCleanerPool.shutdownAndWait();
		} catch (InterruptedException e) {
			String message = "Failed to clean temporay tables.";
			log.error(message);
			log.error(e.getMessage());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// set CORS http headers
			if (wfsConfig.getServer().isEnableCORS())
				addCORSHeaders(request, response, false);

			if (wfsConfig.getOperations().getRequestEncoding().getMethod() == EncodingMethod.XML)
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "KVP encoding of requests is not advertised.");

			Map<String, String> parameters = new HashMap<String, String>();

			// check for invalid parameters
			for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
				String name = entry.getKey().toUpperCase();
				String value = entry.getValue()[0];

				for (int i = 1; i < entry.getValue().length; i++) {
					if (!value.equals(entry.getValue()[i]))
						throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Found inconsistent values for parameter " + name + ": " + Util.collection2string(Arrays.asList(entry.getValue()), ", "));
				}

				if (parameters.containsKey(name)) {
					if (!value.equals(parameters.get(name)))
						throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Found inconsistent values for parameter " + name + ": " + value + ", " + parameters.get(name));
				}

				if (!value.isEmpty())
					parameters.put(name, value);
			}

			String operationName = parameters.get(KVPConstants.REQUEST);
			if (operationName == null)
				throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The request lacks the mandatory " + KVPConstants.REQUEST + " parameter."); 

			KVPRequestReader reader = null;
			if (operationName.equals(KVPConstants.GET_CAPABILITIES))
				reader = new GetCapabilitiesReader(parameters, wfsConfig);
			else if (operationName.equals(KVPConstants.DESCRIBE_FEATURE_TYPE))
				reader = new DescribeFeatureTypeReader(parameters, wfsConfig);
			else if (operationName.equals(KVPConstants.GET_FEATURE))
				reader = new GetFeatureReader(parameters, wfsSchema, cityGMLBuilder, wfsConfig);
			else if (operationName.equals(KVPConstants.LIST_STORED_QUERIES))
				reader = new ListStoredQueriesReader(parameters, wfsConfig);
			else if (operationName.equals(KVPConstants.DESCRIBE_STORED_QUERIES))
				reader = new DescribeStoredQueriesReader(parameters, wfsConfig);

			if (reader == null)
				throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "The operation " + operationName + " is not supported by this WFS implementation.");

			// read request
			Object wfsRequest = reader.readRequest();	
			handleRequest(wfsRequest, operationName, reader.getNamespaces(), request, response);

		} catch (WFSException e) {
			exceptionReportHandler.sendErrorResponse(e, request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// set CORS http headers
			if (wfsConfig.getServer().isEnableCORS())
				addCORSHeaders(request, response, false);

			if (wfsConfig.getOperations().getRequestEncoding().getMethod() == EncodingMethod.KVP)
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "XML encoding of requests is not advertised.");

			Unmarshaller unmarshaller = cityGMLBuilder.getJAXBContext().createUnmarshaller();
			ValidationEventHandlerImpl validationEventHandler = null;

			// support XML validation
			if (wfsConfig.getOperations().getRequestEncoding().isUseXMLValidation()) {
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
			handleRequest(jaxbElement.getValue(), jaxbElement.getName().toString(), namespaceFilter, request, response);

		} catch (JAXBException e) {
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to unmarshal the XML message.", e), request, response); 
		} catch (SAXException e) {			
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "Failed to parse the XML message.", e), request, response); 
		} catch (ParserConfigurationException e) {
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to initialize a SAX parser for parsing the XML message.", e), request, response); 
		} catch (WFSException e) {
			exceptionReportHandler.sendErrorResponse(e, request, response);
		}
	}

	private void handleRequest(Object wfsRequest, String operationName, NamespaceFilter namespaceFilter, HttpServletRequest request, HttpServletResponse response) throws WFSException {
		// check database connection
		if (!connectionPool.isConnected())
			DatabaseConnector.connect(exporterConfig);

		try {
			if (wfsRequest instanceof GetFeatureType) {
				// make sure we only serve a maximum number of requests in parallel
				putRequestOnQueue(request.getRemoteAddr());

				try {
					// handle GetFeature request
					GetFeatureHandler getFeatureHandler = new GetFeatureHandler(cityGMLBuilder, wfsConfig, exporterConfig);
					getFeatureHandler.doOperation((GetFeatureType)wfsRequest, namespaceFilter, request, response);
				} finally {
					// free slot from the request queue
					requestQueue.remove(request.getRemoteAddr());
				}
			}

			else if (wfsRequest instanceof DescribeFeatureTypeType) {
				DescribeFeatureTypeHandler describeFeatureTypeHandler = new DescribeFeatureTypeHandler(wfsConfig);
				describeFeatureTypeHandler.doOperation((DescribeFeatureTypeType)wfsRequest, getServletContext(), request, response);
			}

			else if (wfsRequest instanceof GetCapabilitiesType) {				
				GetCapabilitiesHandler getCapabilitiesHandler = new GetCapabilitiesHandler(cityGMLBuilder, wfsConfig);
				getCapabilitiesHandler.doOperation((GetCapabilitiesType)wfsRequest, getServletContext(), request, response);
			}

			else if (wfsRequest instanceof ListStoredQueriesType) {
				ListStoredQueriesHandler listStoredQueriesHandler = new ListStoredQueriesHandler(cityGMLBuilder, wfsConfig);
				listStoredQueriesHandler.doOperation((ListStoredQueriesType)wfsRequest, request, response);
			}

			else if (wfsRequest instanceof DescribeStoredQueriesType) {
				DescribeStoredQueriesHandler describeStoredQueriesHandler = new DescribeStoredQueriesHandler(wfsConfig);
				describeStoredQueriesHandler.doOperation((DescribeStoredQueriesType)wfsRequest, request, response);
			}

			else if (wfsRequest != null)
				throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "The operation " + operationName + " is not supported by this WFS implementation.");

			else
				throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "Failed to parse the requested operation.");

		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal JAXB error occured whilst processing the request.", e);
		}
	}

	private void putRequestOnQueue(String request) throws WFSException {
		try {
			// make sure we only serve a maximum number of requests in parallel
			if (!requestQueue.offer(request, wfsConfig.getServer().getWaitTimeout(), TimeUnit.SECONDS))
				throw new WFSException(WFSExceptionCode.SERVICE_UNAVAILABLE, "The service is currently unavailable because it is overloaded. " +
						"Generally, this is a temporary state. Please retry later.");
		} catch (InterruptedException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "The service has internally interrupted the request.", e);
		}
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// support CORS preflight requests
		if (wfsConfig.getServer().isEnableCORS())
			addCORSHeaders(request, response, true);
	}
	
	private void addCORSHeaders(HttpServletRequest request, HttpServletResponse response, boolean isOptions) {
		// do nothing if this is not a CORS request
		if (request.getHeader("Origin") == null)
			return;
		
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		// add preflight headers
		if (isOptions && request.getHeader("Access-Control-Request-Method") != null) {			
			response.setHeader("Access-Control-Allow-Methods", "GET, POST");
			response.setHeader("Access-Control-Max-Age", "86400");

			String requestCORSHeaders = request.getHeader("Access-Control-Request-Headers");
			if (requestCORSHeaders != null)
				response.setHeader("Access-Control-Allow-Headers", requestCORSHeaders);
		}	
	}

}
