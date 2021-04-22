package vcs.citydb.wfs.util;

import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.connection.DatabaseConnectionWarning;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.log.Logger;
import org.citydb.util.Util;
import org.opengis.referencing.FactoryException;
import vcs.citydb.wfs.config.Constants;
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
                if (databaseConfig.getActiveConnection() == null) {
                    WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.INTERNAL_SERVER_ERROR);
                    message.addExceptionText("Failed to connect to the database.");
                    message.addExceptionText("No database connection provided in " + Constants.CONFIG_FILE + '.');
                    throw new WFSException(message);
                }

                try {
                    connectionPool.setDatabaseVersionChecker(new DatabaseVersionChecker());
                    connectionPool.connect(databaseConfig.getActiveConnection());
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

}
