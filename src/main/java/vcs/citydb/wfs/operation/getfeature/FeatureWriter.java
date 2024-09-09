package vcs.citydb.wfs.operation.getfeature;

import net.opengis.wfs._2.TruncatedResponse;
import org.citydb.core.operation.exporter.util.Metadata;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;

public interface FeatureWriter extends org.citydb.core.operation.exporter.writer.FeatureWriter {
    void startFeatureCollection(long matchNo, long returnNo, String previous, String next) throws FeatureWriteException;

    void startFeatureCollection(long matchNo, long returnNo) throws FeatureWriteException;

    void endFeatureCollection() throws FeatureWriteException;

    void startAdditionalObjects() throws FeatureWriteException;

    void endAdditionalObjects() throws FeatureWriteException;

    void writeAdditionalObjects() throws FeatureWriteException;

    void writeTruncatedResponse(TruncatedResponse truncatedResponse) throws FeatureWriteException;

    void setWriteSingleFeature(boolean isWriteSingleFeature);

    long setSequentialWriting(boolean useSequentialWriting);

    default void writeHeader() throws FeatureWriteException {
        // nothing to do
    }

    default Metadata getMetadata() {
        // we do not support metadata on the root element
        return null;
    }
}
