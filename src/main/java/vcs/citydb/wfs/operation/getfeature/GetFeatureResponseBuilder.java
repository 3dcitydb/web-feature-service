package vcs.citydb.wfs.operation.getfeature;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.config.Config;

import net.opengis.wfs._2.GetFeatureType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.util.GeometryStripper;

public interface GetFeatureResponseBuilder {
	public String getMimeType();
	public boolean supportsHitsResponse();
	public void initializeContext(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, Map<String, String> formatOptions, GeometryStripper geometryStripper, UIDCacheManager uidCacheManager, Object eventChannel, WFSConfig wfsConfig, Config exporterConfig) throws FeatureWriteException;
	public FeatureWriter buildFeatureWriter(OutputStream stream, String encoding) throws FeatureWriteException;
}
