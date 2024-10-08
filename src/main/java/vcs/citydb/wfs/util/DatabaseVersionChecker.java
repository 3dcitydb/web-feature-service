package vcs.citydb.wfs.util;

import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionWarning;
import org.citydb.core.database.version.DatabaseVersion;
import org.citydb.core.database.version.DatabaseVersionException;
import org.citydb.core.database.version.DatabaseVersionSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseVersionChecker implements org.citydb.core.database.version.DatabaseVersionChecker {
    private final DatabaseVersionSupport[] supportedVersions = new DatabaseVersionSupport[]{
            DatabaseVersionSupport.targetVersion(4, 4, 0).withBackwardsCompatibility(4, 0, 0).withRevisionForwardCompatibility(true),
            DatabaseVersionSupport.targetVersion(3, 3, 1).withBackwardsCompatibility(3, 1, 0).withRevisionForwardCompatibility(true)
    };

    @Override
    public List<DatabaseConnectionWarning> checkVersionSupport(AbstractDatabaseAdapter databaseAdapter) throws DatabaseVersionException {
        // we only check against the 3d city database
        DatabaseVersion version = databaseAdapter.getConnectionMetaData().getCityDBVersion();
        List<DatabaseConnectionWarning> warnings = new ArrayList<>();

        // check for unsupported version
        if (!version.isSupportedBy(supportedVersions)) {
            throw new DatabaseVersionException("The version " + version + " of the " + DatabaseConfig.CITYDB_PRODUCT_NAME + " is not supported.",
                    null, DatabaseConfig.CITYDB_PRODUCT_NAME, Arrays.asList(supportedVersions));
        }

        // check for outdated version
        for (DatabaseVersionSupport supportedVersion : supportedVersions) {
            if (supportedVersion.getTargetVersion().compareTo(version) > 0) {
                warnings.add(new DatabaseConnectionWarning("The version " + version + " of the " + DatabaseConfig.CITYDB_PRODUCT_NAME + " is out of date. Consider upgrading.",
                        null, DatabaseConfig.CITYDB_PRODUCT_NAME, DatabaseConnectionWarning.ConnectionWarningType.OUTDATED_DATABASE_VERSION));
                break;
            }
        }

        return warnings;
    }

    @Override
    public List<DatabaseVersionSupport> getSupportedVersions(String productName) {
        return Arrays.asList(supportedVersions);
    }

}
