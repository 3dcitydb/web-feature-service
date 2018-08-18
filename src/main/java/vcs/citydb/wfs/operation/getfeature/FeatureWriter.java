package vcs.citydb.wfs.operation.getfeature;

import org.citydb.citygml.exporter.writer.FeatureWriteException;

import net.opengis.wfs._2.TruncatedResponse;

public interface FeatureWriter extends org.citydb.citygml.exporter.writer.FeatureWriter {
	public void startFeatureCollection(long matchNo, long returnNo) throws FeatureWriteException;
	public void endFeatureCollection() throws FeatureWriteException;
	public void startAdditionalObjects() throws FeatureWriteException;
	public void endAdditionalObjects() throws FeatureWriteException;
	public void writeAdditionalObjects() throws FeatureWriteException;
	public void writeTruncatedResponse(TruncatedResponse truncatedResponse) throws FeatureWriteException;
	public void setWriteSingleFeature(boolean isWriteSingleFeature);
}
