package vcs.citydb.wfs;

import net.opengis.wfs._2.*;
import org.citydb.config.Config;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.log.Logger;
import org.citydb.util.xml.SecureXMLProcessors;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.xml.schema.SchemaHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.EncodingMethod;
import vcs.citydb.wfs.exception.AccessControlException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionReportHandler;
import vcs.citydb.wfs.kvp.*;
import vcs.citydb.wfs.operation.describefeaturetype.DescribeFeatureTypeHandler;
import vcs.citydb.wfs.operation.getcapabilities.GetCapabilitiesHandler;
import vcs.citydb.wfs.operation.getfeature.GetFeatureHandler;
import vcs.citydb.wfs.operation.getpropertyvalue.GetPropertyValueHandler;
import vcs.citydb.wfs.operation.storedquery.*;
import vcs.citydb.wfs.paging.PageRequest;
import vcs.citydb.wfs.paging.PagingCacheManager;
import vcs.citydb.wfs.paging.PagingHandler;
import vcs.citydb.wfs.security.AccessController;
import vcs.citydb.wfs.util.*;
import vcs.citydb.wfs.util.xml.NamespaceFilter;
import vcs.citydb.wfs.util.xml.ValidationEventHandlerImpl;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@WebServlet(Constants.WFS_SERVICE_PATH)
public class WFSService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger log = Logger.getInstance();
	private final DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();

	private CityGMLBuilder cityGMLBuilder;
	private RequestLimiter limiter;
	private AccessController accessController;
	private WFSConfig wfsConfig;
	private Config config;

	private SAXParserFactory saxParserFactory;
	private Schema wfsSchema;
	private SingleWorkerPool<CacheCleanerWork> cacheCleanerPool;
	private WFSExceptionReportHandler exceptionReportHandler;

	@Override
	public void init() throws ServletException {
		// check whether servlet initialization threw an error
		Object error = getServletContext().getAttribute(Constants.INIT_ERROR_ATTRNAME);
		if (error instanceof ServletException)
			throw (ServletException) error;

		log.info("WFS service is loaded by the servlet container.");

		// service specific initialization
		ObjectRegistry registry = ObjectRegistry.getInstance();
		config = registry.getConfig();
		cityGMLBuilder = registry.getCityGMLBuilder();
		limiter = registry.lookup(RequestLimiter.class);
		accessController = registry.lookup(AccessController.class);
		wfsConfig = registry.lookup(WFSConfig.class);

		exceptionReportHandler = new WFSExceptionReportHandler(cityGMLBuilder);

		try {
			saxParserFactory = SecureXMLProcessors.newSAXParserFactory();
			saxParserFactory.setNamespaceAware(true);
		} catch (Throwable e) {
			String message = "Failed to enable secure processing of XML queries.";
			log.error(message);
			log.error(e.getMessage());
			throw new ServletException(message, e);
		}

		try {
			StoredQueryManager storedQueryManager = new StoredQueryManager(cityGMLBuilder, saxParserFactory, getServletContext().getRealPath(Constants.STORED_QUERIES_PATH), wfsConfig);
			registry.register(storedQueryManager);
		} catch (Throwable e) {
			String message = "Failed to initialize stored query manager.";
			log.error(message);
			log.error(e.getMessage());
			throw new ServletException(message, e);
		}

		// read WFS 2.0 schema to validate requests
		if (wfsConfig.getOperations().getRequestEncoding().isUseXMLValidation()) {
			try {
				SchemaHandler schemaHandler = registry.lookup(SchemaHandler.class);
				schemaHandler.parseSchema(new File(getServletContext().getRealPath(Constants.SCHEMAS_PATH + "/ogc/wfs/2.0.2/wfs.xsd")));
				schemaHandler.parseSchema(new File(getServletContext().getRealPath(Constants.SCHEMAS_PATH + "/ogc/wfs/extensions/wfs-vcs.xsd")));
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				wfsSchema = schemaFactory.newSchema(schemaHandler.getSchemaSources());
			} catch (SAXException e) {
				String message = "Failed to read WFS XML Schema from " + Constants.SCHEMAS_PATH + "/ogc/wfs.";
				log.error(message);
				log.error(e.getMessage());
				throw new ServletException(message, e);
			}
		}

		// register cache cleaner pool
		cacheCleanerPool = new SingleWorkerPool<>(
				"cache_cleaner",
				CacheCleanerWorker::new,
				wfsConfig.getServer().getMaxParallelRequests());

		cacheCleanerPool.prestartCoreWorker();
		registry.register(CacheCleanerWorker.class.getName(), cacheCleanerPool);

		// register paging cache manager
		if (wfsConfig.getConstraints().isUseResultPaging()) {
			PagingCacheManager pagingCacheManager = new PagingCacheManager(cacheCleanerPool, wfsConfig);
			registry.register(pagingCacheManager);
		}
	}

	@Override
	public void destroy() {
		log.info("WFS service is destroyed by the servlet container.");

		// destroy resources which may otherwise cause memory leaks
		try {
			cacheCleanerPool.shutdownAndWait();
		} catch (InterruptedException e) {
			String message = "Failed to shutdown cache cleaner pool.";
			log.error(message);
			log.error(e.getMessage());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// set CORS http headers
		if (wfsConfig.getServer().isEnableCORS())
			addCORSHeaders(request, response, false);

		KVPRequestReader reader = null;
		boolean isPagingRequest = ServerUtil.containsParameter(request, KVPConstants.PAGE_ID);

		try {
			if (wfsConfig.getOperations().getRequestEncoding().getMethod() == EncodingMethod.XML && !isPagingRequest)
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "KVP encoding of requests is not advertised.");

			if (isPagingRequest && !wfsConfig.getConstraints().isUseResultPaging())
				throw new WFSException(WFSExceptionCode.OPTION_NOT_SUPPORTED, "Result paging is not advertised.");

			// parse parameters
			Map<String, String> parameters = new HashMap<>();
			for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
				String name = entry.getKey().toUpperCase();
				String value = entry.getValue()[0];

				for (int i = 1; i < entry.getValue().length; i++) {
					if (!value.equals(entry.getValue()[i]))
						throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Found inconsistent values for parameter " + name + ": " + Util.collection2string(Arrays.asList(entry.getValue()), ", "), name);
				}

				String previous = parameters.get(name);
				if (previous != null && !value.equals(parameters.get(previous)))
					throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Found inconsistent values for parameter " + name + ": " + value + ", " + parameters.get(name), name);

				if (!value.isEmpty())
					parameters.put(name, value);
			}

			String operationName = parameters.get(KVPConstants.REQUEST);
			if (operationName != null) {
				switch (operationName) {
					case KVPConstants.GET_CAPABILITIES:
						reader = new GetCapabilitiesReader(parameters, wfsConfig);
						break;
					case KVPConstants.DESCRIBE_FEATURE_TYPE:
						reader = new DescribeFeatureTypeReader(parameters, wfsConfig);
						break;
					case KVPConstants.GET_PROPERTY_VALUE:
						reader = new GetPropertyValueReader(parameters, wfsSchema, cityGMLBuilder, wfsConfig);
						break;
					case KVPConstants.GET_FEATURE:
						reader = new GetFeatureReader(parameters, wfsSchema, cityGMLBuilder, wfsConfig);
						break;
					case KVPConstants.LIST_STORED_QUERIES:
						reader = new ListStoredQueriesReader(parameters, wfsConfig);
						break;
					case KVPConstants.DESCRIBE_STORED_QUERIES:
						reader = new DescribeStoredQueriesReader(parameters, wfsConfig);
						break;
					case KVPConstants.DROP_STORED_QUERY:
						reader = new DropStoredQueryReader(parameters, wfsConfig);
						break;
					case KVPConstants.CREATE_STORED_QUERY:
					default:
						throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The operation " + operationName + " is not supported by this WFS implementation.", KVPConstants.REQUEST);
				}
			} else if (isPagingRequest) {
				reader = new PagingReader(parameters, wfsConfig);
			} else
				throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, "The request lacks the mandatory " + KVPConstants.REQUEST + " parameter.", KVPConstants.REQUEST);

			// parse and process request
			Object wfsRequest = reader.readRequest();
			handleRequest(wfsRequest, reader.getOperationName(), reader.getNamespaces(), request, response);
		} catch (WFSException e) {
			String operationName = reader != null ? reader.getOperationName() : null;
			exceptionReportHandler.sendErrorResponse(e, operationName, request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// set CORS http headers
		if (wfsConfig.getServer().isEnableCORS())
			addCORSHeaders(request, response, false);

		String operationName = null;
		try {
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
			Object object = unmarshallerHandler.getResult();
			if (!(object instanceof JAXBElement<?>))
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Failed to parse XML document received through HTTP POST.", KVPConstants.REQUEST);

			JAXBElement<?> element = (JAXBElement<?>) object;
			operationName = element.getName().getLocalPart();

			// process request
			handleRequest(element.getValue(), operationName, namespaceFilter, request, response);
		} catch (JAXBException | SAXException | ParserConfigurationException e) {
			exceptionReportHandler.sendErrorResponse(new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "Failed to parse the XML message.", e), operationName, request, response);
		} catch (WFSException e) {
			exceptionReportHandler.sendErrorResponse(e, operationName, request, response);
		}
	}

	private void handleRequest(Object wfsRequest, String operationName, NamespaceFilter namespaceFilter, HttpServletRequest request, HttpServletResponse response) throws WFSException {
		// check access permission
		try {
			accessController.requireAccess(operationName, request);
		} catch (AccessControlException e) {
			log.error("Access denied: " + e.getMessage());
			if (e.getCause() != null)
				log.logStackTrace(e.getCause());

			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Access denied for client '" + request.getRemoteHost() + "'.");
		}

		// check database connection
		if (!connectionPool.isConnected())
			DatabaseConnector.connect(config);

		try {
			if (wfsRequest instanceof GetFeatureType) {
				// make sure we only serve a maximum number of requests in parallel
				limiter.requireServiceSlot(request, operationName);
				GetFeatureHandler getFeatureHandler = new GetFeatureHandler(cityGMLBuilder, wfsConfig, config);
				getFeatureHandler.doOperation((GetFeatureType) wfsRequest, namespaceFilter, request, response);
			}

			else if (wfsRequest instanceof GetPropertyValueType) {
				// make sure we only serve a maximum number of requests in parallel
				limiter.requireServiceSlot(request, operationName);
				GetPropertyValueHandler getPropertyValueHandler = new GetPropertyValueHandler(cityGMLBuilder, wfsConfig, config);
				getPropertyValueHandler.doOperation((GetPropertyValueType) wfsRequest, namespaceFilter, request, response);
			}

			else if (wfsRequest instanceof DescribeFeatureTypeType) {
				DescribeFeatureTypeHandler describeFeatureTypeHandler = new DescribeFeatureTypeHandler(wfsConfig);
				describeFeatureTypeHandler.doOperation((DescribeFeatureTypeType)wfsRequest, getServletContext(), request, response);
			}

			else if (wfsRequest instanceof GetCapabilitiesType) {
				GetCapabilitiesHandler getCapabilitiesHandler = new GetCapabilitiesHandler(cityGMLBuilder, wfsConfig);
				getCapabilitiesHandler.doOperation((GetCapabilitiesType)wfsRequest, request, response);
			}

			else if (wfsRequest instanceof ListStoredQueriesType) {
				ListStoredQueriesHandler listStoredQueriesHandler = new ListStoredQueriesHandler(cityGMLBuilder, wfsConfig);
				listStoredQueriesHandler.doOperation((ListStoredQueriesType)wfsRequest, request, response);
			}

			else if (wfsRequest instanceof DescribeStoredQueriesType) {
				DescribeStoredQueriesHandler describeStoredQueriesHandler = new DescribeStoredQueriesHandler(wfsConfig);
				describeStoredQueriesHandler.doOperation((DescribeStoredQueriesType)wfsRequest, request, response);
			}

			else if (wfsRequest instanceof CreateStoredQueryType) {
				CreateStoredQueryHandler createStoredQueriesHandler = new CreateStoredQueryHandler(cityGMLBuilder, wfsConfig);
				createStoredQueriesHandler.doOperation((CreateStoredQueryType)wfsRequest, namespaceFilter, request, response);
			}

			else if (wfsRequest instanceof DropStoredQueryType) {
				DropStoredQueryHandler dropStoredQueriesHandler = new DropStoredQueryHandler(cityGMLBuilder, wfsConfig);
				dropStoredQueriesHandler.doOperation((DropStoredQueryType)wfsRequest, request, response);
			}

			else if (wfsRequest instanceof PageRequest) {
				// make sure we only serve a maximum number of requests in parallel
				limiter.requireServiceSlot(request, operationName);
				PagingHandler pagingHandler = new PagingHandler(cityGMLBuilder, wfsConfig, config);
				pagingHandler.doOperation((PageRequest) wfsRequest, request, response);
			}

			else
				throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "The operation " + operationName + " is not supported by this WFS implementation.", operationName);

		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal JAXB error occurred whilst processing the request.", operationName, e);
		} finally {
			// release slot from limiter
			limiter.releaseServiceSlot(request);
		}
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
