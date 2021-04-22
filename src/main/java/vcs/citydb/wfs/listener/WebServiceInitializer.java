package vcs.citydb.wfs.listener;

import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.exporter.ExportConfig;
import org.citydb.config.project.global.GlobalConfig;
import org.citydb.config.project.global.LogFileMode;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SchemaMappingException;
import org.citydb.database.schema.mapping.SchemaMappingValidationException;
import org.citydb.database.schema.util.SchemaMappingUtil;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util.URLClassLoader;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.model.citygml.ade.binding.ADEContext;
import org.citygml4j.xml.schema.SchemaHandler;
import org.geotools.referencing.CRS;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.util.WeakCollectionCleaner;
import org.xml.sax.SAXException;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.WFSConfigLoader;
import vcs.citydb.wfs.config.logging.ConsoleLog;
import vcs.citydb.wfs.config.logging.FileLog;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.util.DatabaseConnector;
import vcs.citydb.wfs.util.RequestLimiter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@WebListener
public class WebServiceInitializer implements ServletContextListener {
	private final Logger log = Logger.getInstance();
	private final URLClassLoader classLoader = new URLClassLoader(getClass().getClassLoader());

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		ADEExtensionManager adeManager = ADEExtensionManager.getInstance();
		ObjectRegistry registry = ObjectRegistry.getInstance();
		Config config = registry.getConfig();

		SchemaMapping schemaMapping;
		CityGMLBuilder cityGMLBuilder;
		WFSConfig wfsConfig;

		// read 3DCityDB schema mapping and register with object registry
		try {
			schemaMapping = SchemaMappingUtil.getInstance().unmarshal(CoreConstants.CITYDB_SCHEMA_MAPPING_FILE);
			registry.setSchemaMapping(schemaMapping);
		} catch (JAXBException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to read 3DCityDB schema mapping file.", e));
			return;
		} catch (SchemaMappingException | SchemaMappingValidationException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("The 3DCityDB schema mapping file is invalid.", e));
			return;
		}

		// load ADE extensions
		String adeExtensionsPath = Paths.get(Constants.ADE_EXTENSIONS_PATH).isAbsolute() ?
				Constants.ADE_EXTENSIONS_PATH :
				context.getRealPath(Constants.ADE_EXTENSIONS_PATH);

		if (Files.exists(Paths.get(adeExtensionsPath))) {
			try (Stream<Path> stream = Files.walk(Paths.get(adeExtensionsPath))
					.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))) {
				stream.forEach(classLoader::addPath);
				adeManager.loadExtensions(classLoader);
				adeManager.loadSchemaMappings(schemaMapping);
			} catch (IOException e) {
				context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize ADE extension support.", e));
				return;
			}
		}

		// initialize JAXB context for CityGML, ADE and WFS schemas and register with object registry
		try {
			CityGMLContext cityGMLContext = CityGMLContext.getInstance();
			for (ADEContext adeContext : adeManager.getADEContexts()) {
				cityGMLContext.registerADEContext(adeContext);
			}

			cityGMLBuilder = cityGMLContext.createCityGMLBuilder(classLoader, "net.opengis.wfs._2", "net.opengis.ows._1", "net.opengis.fes._2");
			registry.setCityGMLBuilder(cityGMLBuilder);
		} catch (CityGMLBuilderException | ADEException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize citygml4j context.", e));
			return;
		}

		// initialize and register CityGML XML schema handler
		try {
			SchemaHandler schemaHandler = SchemaHandler.newInstance(); 
			registry.register(schemaHandler);
		} catch (SAXException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize CityGML XML schema parser.", e));
			return;
		}

		// load WFS configuration file and register with object registry
		try {
			wfsConfig = WFSConfigLoader.load(context);
			registry.register(wfsConfig);
		} catch (JAXBException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to load WFS config from " + Constants.CONFIG_FILE + '.'));
			return;
		}

		// create request limiter and register with object registry
		registry.register(new RequestLimiter(wfsConfig));

		// create 3DCityDB dummy configuration and register with object registry
		initConfig(config, wfsConfig);

		// initialize logging
		try {			
			initLogging(wfsConfig, context);
		} catch (ServletException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, e);
			registry.getEventDispatcher().shutdownNow();
			return;
		}

		// initialize database connection pool
		try {
			DatabaseConnector.connect(config);
		} catch (WFSException e) {
			e.getExceptionMessages().forEach(msg -> msg.getExceptionTexts().forEach(log::error));
		}
		
		// log ADE exceptions
		if (adeManager.hasExceptions()) {
			adeManager.logExceptions();
		}
		
		// preprocess advertised CityGML and ADE feature types
		wfsConfig.getFeatureTypes().preprocessFeatureTypes();

		// geotools - don't allow the connection to the EPSG database to time out
		System.setProperty("org.geotools.epsg.factory.timeout", "-1");
		CRS.cleanupThreadLocals();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		ObjectRegistry registry = ObjectRegistry.getInstance();

		// remove initialization error
		context.removeAttribute(Constants.INIT_ERROR_ATTRNAME);

		// disconnect connection pool
		DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();
		if (connectionPool.isConnected()) {
			connectionPool.purge();
			connectionPool.disconnect();
		}

		// shutdown event dispatcher thread
		if (registry.getEventDispatcher() != null) {
			try {
				registry.getEventDispatcher().shutdownAndWait();
			} catch (InterruptedException e) {
				registry.getEventDispatcher().shutdownNow();
			}
		}

		// deregister JDBC drivers that were not loaded through the connection pool		
		ClassLoader loader = getClass().getClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		Set<Driver> driversToUnload = new HashSet<>();

		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			ClassLoader driverLoader = driver.getClass().getClassLoader();
			if (loader.equals(driverLoader))
				driversToUnload.add(driver);
		}

		for (Driver driver : driversToUnload) {
			try {
				DriverManager.deregisterDriver(driver);
				log.info("Unregistered JDBC driver " + driver);
			} catch(Exception e) {
				log.error("Could now unload driver " + driver.getClass() + " " + e.getMessage());
				e.printStackTrace();
			}
		}

		// some geotools related stuff
		WeakCollectionCleaner.DEFAULT.exit();
		CRS.cleanupThreadLocals();
		DeferredAuthorityFactory.exit();

		// free classloader resources
		try {
			classLoader.close();
		} catch (IOException e) {
			//
		}

		// detach log file
		log.detachLogFile();
	}

	private void initConfig(Config config, WFSConfig wfsConfig) {
		// database settings
		DatabaseConfig databaseConfig = config.getDatabaseConfig();
		databaseConfig.setActiveConnection(wfsConfig.getDatabase().getConnection());
		databaseConfig.setReferenceSystems(wfsConfig.getDatabase().getReferenceSystems());

		// global settings
		GlobalConfig globalConfig = config.getGlobalConfig();
		globalConfig.setCache(wfsConfig.getServer().getTempCache());

		// export settings
		ExportConfig exportConfig = config.getExportConfig();
		exportConfig.getContinuation().setExportCityDBMetadata(wfsConfig.getConstraints().isExportCityDBMetadata());
		exportConfig.getCityObjectGroup().setExportMemberAsXLinks(true);
		exportConfig.getAppearances().setExportAppearances(false);
		exportConfig.getAppearances().setExportTextureFiles(false);
	}

	private void initLogging(WFSConfig wfsConfig, ServletContext context) throws ServletException {
		FileLog fileLog = wfsConfig.getLogging().getFile();
		ConsoleLog consoleLog = wfsConfig.getLogging().getConsole();

		// try and read log filename from configuration file
		Path logFile;
		if (fileLog.getFileName() != null) {
			logFile = Paths.get(fileLog.getFileName());
			if (Files.isDirectory(logFile)) {
				logFile = logFile.resolve(Constants.LOG_FILE);
			}
		} else {
			logFile = Paths.get(Constants.LOG_FILE);
		}

		// choose default log filename if we did not succeed
		if (!logFile.isAbsolute()) {
			String logPath = context.getRealPath(Constants.LOG_PATH);
			if (logPath == null) {
				throw new ServletException("Failed to access local log path at '" + Constants.LOG_PATH + "'.");
			}

			logFile = Paths.get(logPath).resolve(logFile);
		}

		// log to console
		log.enableConsoleLogging(consoleLog != null);
		if (consoleLog != null)
			log.setConsoleLogLevel(consoleLog.getLogLevel());

		// log to file
		log.setFileLogLevel(fileLog.getLogLevel());
		log.appendLogFile(logFile, LogFileMode.APPEND);
	}
}
