package vcs.citydb.wfs.operation.getfeature;

import org.citydb.citygml.exporter.util.Metadata;
import org.citydb.citygml.exporter.writer.FeatureWriteException;

import net.opengis.wfs._2.TruncatedResponse;

public interface FeatureWriter extends org.citydb.citygml.exporter.writer.FeatureWriter {
	void startFeatureCollection(long matchNo, long returnNo) throws FeatureWriteException;
	void endFeatureCollection() throws FeatureWriteException;
	void startAdditionalObjects() throws FeatureWriteException;
	void endAdditionalObjects() throws FeatureWriteException;
	void writeAdditionalObjects() throws FeatureWriteException;
	void writeTruncatedResponse(TruncatedResponse truncatedResponse) throws FeatureWriteException;
	void setWriteSingleFeature(boolean isWriteSingleFeature);

	default void writeHeader() throws FeatureWriteException {
		// nothing to do
	}

	default Metadata getMetadata() {
		// we do not support metadata on the root element
		return null;
	}
}
