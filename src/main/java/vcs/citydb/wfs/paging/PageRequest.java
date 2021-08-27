package vcs.citydb.wfs.paging;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.util.ServerUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public abstract class PageRequest {
    private PageObject pageObject;
    private WFSConfig wfsConfig;
    private long pageNumber;

    PageRequest() {
    }

    PageRequest(PageObject pageObject, WFSConfig wfsConfig) {
        this.pageObject = pageObject;
        this.wfsConfig = wfsConfig;
    }

    public static PageRequest dummy() {
        return DummyRequest.getInstance();
    }

    abstract PageRequest newInstance(long pageNumber, String identifier);
    abstract int size();
    abstract long[] getValues();
    abstract void setValues(long[] values);
    abstract void setDefaultValues();
    public abstract String getOperationName();

    PageRequest generateRequestFor(long pageNumber) {
        PageRequest pageRequest = newInstance(pageNumber, pageObject.getIdentifier());
        pageRequest.pageObject = pageObject;
        pageRequest.wfsConfig = wfsConfig;
        pageRequest.pageNumber = pageNumber;

        return pageRequest;
    }

    String getIdentifier() {
        return pageObject.getIdentifier();
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public String first(HttpServletRequest request) throws WFSException {
        return generateURL(0, request);
    }

    public String next(HttpServletRequest request) throws WFSException {
        return generateURL(pageNumber + 1, request);
    }

    public String previous(HttpServletRequest request) throws WFSException {
        return generateURL(pageNumber - 1, request);
    }

    private String generateURL(long pageNumber, HttpServletRequest request) throws WFSException {
        long[] identifierBits = pageObject.getIdentifierBits();
        String pageId = pageObject.getIdentifier() + "-" + Long.toHexString(identifierBits[0] ^ identifierBits[1] ^ pageNumber);
        String serviceURL = wfsConfig.getServer().isSetExternalServiceURL() ?
                wfsConfig.getServer().getExternalServiceURL() :
                ServerUtil.getServiceURL(request);

        return serviceURL + Constants.WFS_SERVICE_PATH + "?" + KVPConstants.PAGE_ID + "=" + pageId;
    }

    void cacheValues() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(pageObject.getTempFile().toFile(), "rwd");
             FileChannel channel = file.getChannel()) {
            long[] values = getValues();
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * values.length);
            for (long value : values)
                buffer.putLong(value);

            buffer.flip();
            channel.position(Long.BYTES * values.length * pageNumber);
            channel.write(buffer);

            pageObject.setSerialized(true);
        } catch (Throwable e) {
            throw new IOException("Failed to cache values to temporary file.", e);
        }
    }

    void updateValues() throws IOException {
        if (pageNumber == 0) {
            setDefaultValues();
        } else {
            try (RandomAccessFile file = new RandomAccessFile(pageObject.getTempFile().toFile(), "r");
                 FileChannel channel = file.getChannel()) {
                long[] values = new long[size()];
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * size());
                channel.position(Long.BYTES * size() * (pageNumber - 1));
                channel.read(buffer);

                buffer.flip();
                buffer.asLongBuffer().get(values);
                setValues(values);
            } catch (Throwable e) {
                throw new IOException("Failed to read cache values from temporary file.", e);
            }
        }
    }
}
