package vcs.citydb.wfs.operation.describefeaturetype;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.servlet.ServletContext;

import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import vcs.citydb.wfs.exception.SchemaReaderException;

public interface SchemaReader {
	public String getMimeType();
	public void initializeContext(Set<FeatureType> featureTypes, CityGMLVersion version, ServletContext servletContext) throws SchemaReaderException;
	public InputStream openSchema() throws SchemaReaderException, IOException;
}
