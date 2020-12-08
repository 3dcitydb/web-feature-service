package vcs.citydb.wfs.listener;

import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.ProjectConfig;
import org.citydb.config.project.common.XSLTransformation;
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
import vcs.citydb.wfs.config.WFSConfigListener;
import vcs.citydb.wfs.config.system.ConsoleLog;
import vcs.citydb.wfs.config.system.FileLog;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.util.DatabaseConnector;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@WebListener
public class WebServiceInitializer implements ServletContextListener {
	private final Logger log = Logger.getInstance();
	private URLClassLoader classLoader = new URLClassLoader(getClass().getClassLoader());

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		ObjectRegistry registry = ObjectRegistry.getInstance();
		ADEExtensionManager adeManager = ADEExtensionManager.getInstance();

		SchemaMapping schemaMapping;
		CityGMLBuilder cityGMLBuilder;
		WFSConfig wfsConfig;
		Config config;

		// read 3DCityDB schema mapping and register with ObjectRegistry
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
		if (Files.exists(Paths.get(context.getRealPath(Constants.ADE_EXTENSIONS_PATH)))) {
			try (Stream<Path> stream = Files.walk(Paths.get(context.getRealPath(Constants.ADE_EXTENSIONS_PATH)))
					.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))) {
				stream.forEach(path -> classLoader.addPath(path));
				adeManager.loadExtensions(classLoader);
				adeManager.loadSchemaMappings(schemaMapping);
			} catch (IOException e) {
				context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize ADE extension support.", e));
				return;
			}
		}

		// initialize JAXB context for CityGML, ADE and WFS schemas and register with ObjectRegistry
		try {
			CityGMLContext cityGMLContext = CityGMLContext.getInstance();
			for (ADEContext adeContext : adeManager.getADEContexts())
				cityGMLContext.registerADEContext(adeContext);

			cityGMLBuilder = cityGMLContext.createCityGMLBuilder(classLoader, "net.opengis.wfs._2", "net.opengis.ows._1", "net.opengis.fes._2");
			registry.setCityGMLBuilder(cityGMLBuilder);
		} catch (CityGMLBuilderException | ADEException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize citygml4j context.", e));
			return;
		}

		// initialize and register CityGML XML schema handler
		try {
			SchemaHandler schemaHandler = SchemaHandler.newInstance(); 
			registry.register(SchemaHandler.class.getName(), schemaHandler);
		} catch (SAXException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize CityGML XML schema parser.", e));
			return;
		}

		// read WFS configuration file and register with ObjectRegistry
		try {
			JAXBContext configContext = JAXBContext.newInstance(WFSConfig.class);
			Unmarshaller um = configContext.createUnmarshaller();
			um.setListener(new WFSConfigListener());

			Object wfs = um.unmarshal(context.getResourceAsStream(Constants.CONFIG_PATH + '/' + Constants.CONFIG_FILE));
			if (wfs instanceof WFSConfig) {
				wfsConfig = (WFSConfig)wfs;
				registry.register(WFSConfig.class.getName(), wfs);
			} else {
				context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to load WFS config from " + Constants.CONFIG_PATH + '/' + Constants.CONFIG_FILE + '.'));
				return;
			}

			// adapt paths to XSLT stylesheets if required
			XSLTransformation xslTransformation = wfsConfig.getPostProcessing().getXSLTransformation();
			if (xslTransformation.isEnabled() && xslTransformation.isSetStylesheets()) {
				xslTransformation.getStylesheets().replaceAll(stylesheet -> !Paths.get(stylesheet).isAbsolute() ?
						context.getRealPath(Constants.XSLT_STYLESHEETS_PATH + "/" + stylesheet) : stylesheet);
			}
		} catch (JAXBException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to parse WFS config file.", e));
			return;
		}

		// create 3DCityDB dummy configuration and register with ObjectRegistry
        config = initConfig(wfsConfig);
		registry.register(Config.class.getName(), config);

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
		if (adeManager.hasExceptions())
			adeManager.logExceptions();
		
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

	private Config initConfig(WFSConfig wfsConfig) {
		// database settings
		DatabaseConfig database = new DatabaseConfig();
		database.setActiveConnection(wfsConfig.getDatabase().getConnection());
		database.setReferenceSystems(wfsConfig.getDatabase().getReferenceSystems());

		// global settings
		GlobalConfig global = new GlobalConfig();
		global.setCache(wfsConfig.getUIDCache());

		// export settings
		ExportConfig exporter = new ExportConfig();
		exporter.getContinuation().setExportCityDBMetadata(wfsConfig.getOperations().isExportCityDBMetadata());
		exporter.getCityObjectGroup().setExportMemberAsXLinks(true);
		exporter.getAppearances().setExportAppearances(false);
		exporter.getAppearances().setExportTextureFiles(false);

		return new Config(new ProjectConfig(database, null, exporter, null, null, global), null);
	}

	private void initLogging(WFSConfig wfsConfig, ServletContext context) throws ServletException {
		FileLog fileLog = wfsConfig.getLogging().getFile();
		ConsoleLog consoleLog = wfsConfig.getLogging().getConsole();
		String logFileName = null;

		// try and read log filename from configuration file
		if (fileLog.getFileName() != null) {
			File file = new File(fileLog.getFileName());
			if (file.isDirectory())
				file = new File(fileLog.getFileName(), Constants.LOG_FILE);

			if (file.getParentFile().exists())
				logFileName = file.getAbsolutePath();
		}

		// choose default log filename if we did not succeed
		if (logFileName == null) {
			String logPath = context.getRealPath(Constants.LOG_PATH);
			if (logPath == null) 
				throw new ServletException("Failed to access local log path at '" + Constants.LOG_PATH + "'.");

			logFileName = logPath + File.separator + Constants.LOG_FILE;
		}

		// log to console
		if (consoleLog != null)
			log.setConsoleLogLevel(consoleLog.getLogLevel());

		// log to file
		log.setFileLogLevel(fileLog.getLogLevel());
		log.appendLogFile(Paths.get(logFileName), LogFileMode.APPEND);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info("*** Starting new log file session on " + dateFormat.format(new Date()) + " ***");		
	}

}
