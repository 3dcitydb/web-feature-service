package vcs.citydb.wfs.operation.getfeature;

import net.opengis.wfs._2.GetFeatureType;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.exporter.util.InternalConfig;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.config.Config;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.util.GeometryStripper;

import java.io.Writer;
import java.util.List;
import java.util.Map;

public interface GetFeatureResponseBuilder {
	String getMimeType();
	boolean supportsHitsResponse();
	void initializeContext(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, Map<String, String> formatOptions,
						   GeometryStripper geometryStripper, UIDCacheManager uidCacheManager, Object eventChannel,
						   InternalConfig internalConfig, WFSConfig wfsConfig, Config config) throws FeatureWriteException;
	FeatureWriter buildFeatureWriter(Writer writer) throws FeatureWriteException;
}
