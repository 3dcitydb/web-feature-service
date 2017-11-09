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
package vcs.citydb.wfs.listener;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.citydb.api.database.DatabaseConfigurationException;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseVersionException;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.global.LanguageType;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.xml.schema.SchemaHandler;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.system.ConsoleLog;
import vcs.citydb.wfs.config.system.FileLog;

@WebListener
public class WebServiceInitializer implements ServletContextListener {
	private final Logger log = Logger.getInstance();

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		ObjectRegistry registry = ObjectRegistry.getInstance();
		WFSConfig wfsConfig = null;
		Config exporterConfig = null;

		// initialize JAXB context for CityGML and WFS schemas and register with ObjectRegistry
		try {
			JAXBBuilder jaxbBuilder = new JAXBBuilder("net.opengis.wfs._2", "net.opengis.ows._1", "net.opengis.fes._2");
			registry.register(JAXBBuilder.class.getName(), jaxbBuilder);
		} catch (JAXBException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize JAXB context.", e));
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
			Object wfs = um.unmarshal(event.getServletContext().getResourceAsStream(Constants.CONFIG_PATH + '/' + Constants.CONFIG_FILE));
			if (wfs instanceof WFSConfig) {
				wfsConfig = (WFSConfig)wfs;
				registry.register(WFSConfig.class.getName(), wfs);
			} else {
				context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to read WFS config from " + Constants.CONFIG_PATH + '/' + Constants.CONFIG_FILE + '.'));
				return;
			}
		} catch (JAXBException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, new ServletException("Failed to initialize JAXB context for WFS config file.", e));
			return;
		}

		// create 3DCityDB configuration file and register with ObjectRegistry
		exporterConfig = new Config();
		registry.register(Config.class.getName(), exporterConfig);

		// map from WFS to 3DCityDB configuration
		exporterConfig.getProject().getDatabase().setActiveConnection(wfsConfig.getDatabase().getConnection());
		exporterConfig.getProject().getGlobal().setCache(wfsConfig.getUIDCache());

		// init internationalized labels 
		LanguageType lang = LanguageType.fromValue(System.getProperty("user.language"));
		exporterConfig.getProject().getGlobal().setLanguage(lang);
		Language.I18N = ResourceBundle.getBundle("org.citydb.config.language.Label", new Locale(lang.value()));

		// start new event dispatcher thread
		registry.setEventDispatcher(new EventDispatcher());

		try {			
			// initialize logging and database connection pool
			initLogging(wfsConfig, context);
			initDatabaseConnectionPool(exporterConfig);		
		} catch (ServletException e) {
			context.setAttribute(Constants.INIT_ERROR_ATTRNAME, e);
			registry.getEventDispatcher().shutdownNow();
			registry.cleanup();
			return;
		}
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

			registry.cleanup();
		}

		// detach log file
		log.detachLogFile();
	}

	private void initLogging(WFSConfig wfsConfig, ServletContext context) throws ServletException {
		FileLog fileLog = wfsConfig.getLogging().getFile();
		ConsoleLog consoleLog = wfsConfig.getLogging().getConsole();
		String logFileName = null;

		// try and read log filename from configuration file
		if (fileLog.getFileName() != null) {
			File file = new File(fileLog.getFileName());
			if (file.isDirectory())
				file = new File(fileLog.getFileName() + "/wfs.log");

			if (file.getParentFile().exists())
				logFileName = file.getAbsolutePath();
		}

		// choose default log filename if we did not succeed
		if (logFileName == null) {
			String logPath = context.getRealPath(Constants.LOG_PATH);
			if (logPath == null) 
				throw new ServletException("Failed to access local log path at '" + Constants.LOG_PATH + "'.");

			logFileName = logPath + File.separator + "wfs.log";
		}
		
		// log to console
		log.logToConsole(consoleLog != null);
		if (consoleLog != null)
			log.setDefaultConsoleLogLevel(consoleLog.getLogLevel());

		// log to file
		log.setDefaultFileLogLevel(fileLog.getLogLevel());
		log.appendLogFile(logFileName, false);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info("*** Starting new log file session on " + dateFormat.format(new Date()) + " ***");		
	}

	private void initDatabaseConnectionPool(Config exporterConfig) throws ServletException {
		Database databaseConfig = exporterConfig.getProject().getDatabase();
		if (databaseConfig.getActiveConnection() == null) {
			String message = "No database connection provided in " + Constants.CONFIG_PATH + '/' + Constants.CONFIG_FILE + '.';
			log.error(message);
			throw new ServletException(message);
		}

		DBConnection connection = databaseConfig.getActiveConnection();
		connection.setInternalPassword(connection.getPassword());		

		DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();
		try {
			connectionPool.connect(exporterConfig);
		} catch (DatabaseConfigurationException | SQLException | DatabaseVersionException e) {
			String message = "Failed to connect to database.";
			log.error(message);
			log.error(e.getMessage());
			throw new ServletException(message, e);
		}

		log.info("Database connection established.");
		connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().printToConsole();
		
		// log whether user-defined SRSs are supported
		for (DatabaseSrs refSys : exporterConfig.getProject().getDatabase().getReferenceSystems()) {
			if (refSys.isSupported())
				log.info("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
			else
				log.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
		}
	}
}
