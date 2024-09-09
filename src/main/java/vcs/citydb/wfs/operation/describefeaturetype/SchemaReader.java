package vcs.citydb.wfs.operation.describefeaturetype;

import org.citydb.core.database.schema.mapping.FeatureType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import vcs.citydb.wfs.exception.SchemaReaderException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface SchemaReader {
    String getMimeType();

    void initializeContext(Set<FeatureType> featureTypes, CityGMLVersion version, ServletContext servletContext) throws SchemaReaderException;

    InputStream openSchema() throws SchemaReaderException, IOException;
}
