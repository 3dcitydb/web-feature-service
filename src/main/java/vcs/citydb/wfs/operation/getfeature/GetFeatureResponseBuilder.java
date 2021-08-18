package vcs.citydb.wfs.operation.getfeature;

import net.opengis.wfs._2.GetFeatureType;
import org.citydb.config.Config;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.util.GeometryStripper;

import java.io.Writer;
import java.util.List;
import java.util.Map;

public interface GetFeatureResponseBuilder {
	String getMimeType();
	boolean supportsHitsResponse();
	void initializeContext(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, Map<String, String> formatOptions,
						   GeometryStripper geometryStripper, IdCacheManager idCacheManager, Object eventChannel,
						   InternalConfig internalConfig, WFSConfig wfsConfig, Config config) throws FeatureWriteException;
	FeatureWriter buildFeatureWriter(Writer writer) throws FeatureWriteException;
}
