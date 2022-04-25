package vcs.citydb.wfs.util;

import org.citydb.config.Config;
import org.citydb.config.project.database.*;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.connection.DatabaseConnectionWarning;
import org.citydb.core.database.version.DatabaseVersionException;
import org.citydb.core.util.CoreConstants;
import org.citydb.core.util.Util;
import org.citydb.util.log.Logger;
import org.opengis.referencing.FactoryException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseConnector {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void connect(Config config) throws WFSException {
        DatabaseConnectionPool connectionPool = DatabaseConnectionPool.getInstance();
        if (connectionPool.isConnected())
            return;

        if (lock.tryLock()) {
            try {
                if (connectionPool.isConnected())
                    return;

                Logger log = Logger.getInstance();
                DatabaseConfig databaseConfig = config.getDatabaseConfig();

                try {
                    connectionPool.setDatabaseVersionChecker(new DatabaseVersionChecker());
                    connectionPool.connect(getDatabaseConnection(databaseConfig));
                } catch (DatabaseConfigurationException | SQLException e) {
                    throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to connect to the database.", e);
                } catch (DatabaseVersionException e) {
                    WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INTERNAL_SERVER_ERROR);
                    message.addExceptionText("Failed to connect to the database.");
                    message.addExceptionText(e.getMessage());
                    message.addExceptionText("Supported versions are '" + Util.collection2string(e.getSupportedVersions(), ", ") + "'.");
                    throw new WFSException(message);
                }

                log.info("Database connection established.");
                AbstractDatabaseAdapter adapter = connectionPool.getActiveDatabaseAdapter();
                adapter.getConnectionMetaData().printToConsole();

                // load internal representation for database and user-defined SRSs
                try {
                    adapter.getUtil().decodeDatabaseSrs(adapter.getConnectionMetaData().getReferenceSystem());
                } catch (FactoryException e) {
                    WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INTERNAL_SERVER_ERROR);
                    message.addExceptionText("Failed to retrieve SRS information for '" + adapter.getConnectionMetaData().getReferenceSystem() + "'.");
                    connectionPool.disconnect();
                    throw new WFSException(message);
                }

                // log whether user-defined SRSs are supported
                for (DatabaseSrs refSys : databaseConfig.getReferenceSystems()) {
                    try {
                        adapter.getUtil().decodeDatabaseSrs(refSys);
                    } catch (FactoryException e) {
                        log.error(e.getMessage());
                        refSys.setSupported(false);
                    }

                    if (refSys.isSupported())
                        log.info("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
                    else
                        log.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
                }

                // log connection warnings
                List<DatabaseConnectionWarning> warnings = adapter.getConnectionWarnings();
                if (!warnings.isEmpty()) {
                    for (DatabaseConnectionWarning warning : warnings)
                        log.warn(warning.getMessage());
                }
            } finally {
                lock.unlock();
            }
        } else {
            WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.SERVICE_UNAVAILABLE);
            message.addExceptionText("The service is currently trying to connect to the database.");
            message.addExceptionText("This is a temporary state. Please retry later.");
            throw new WFSException(message);
        }
    }

    private static DatabaseConnection getDatabaseConnection(DatabaseConfig databaseConfig) {
        DatabaseConnection connection = databaseConfig.getActiveConnection() != null ?
                databaseConfig.getActiveConnection() :
                new DatabaseConnection();

        // get connection details from environment variables
        String host = System.getenv(CoreConstants.ENV_CITYDB_HOST);
        String port = System.getenv(CoreConstants.ENV_CITYDB_PORT);
        String name = System.getenv(CoreConstants.ENV_CITYDB_NAME);
        String schema = System.getenv(CoreConstants.ENV_CITYDB_SCHEMA);
        String username = System.getenv(CoreConstants.ENV_CITYDB_USERNAME);
        String password = System.getenv(CoreConstants.ENV_CITYDB_PASSWORD);
        DatabaseType type = System.getenv(CoreConstants.ENV_CITYDB_TYPE) != null ?
                DatabaseType.fromValue(System.getenv(CoreConstants.ENV_CITYDB_TYPE)) :
                null;

        // replace values from config with environment variables
        if (type != null) {
            connection.setDatabaseType(type);
        }

        if (host != null) {
            connection.setServer(host);
        }

        if (port != null) {
            try {
                connection.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                //
            }
        }

        if (name != null) {
            connection.setSid(name);
        }

        if (schema != null) {
            connection.setSchema(schema);
        }

        if (username != null) {
            connection.setUser(username);
        }

        if (password != null) {
            connection.setPassword(password);
        }

        return connection;
    }
}
