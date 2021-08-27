package vcs.citydb.wfs.paging;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

class DummyRequest extends PageRequest {
    private static final DummyRequest instance = new DummyRequest();

    public static DummyRequest getInstance() {
        return instance;
    }

    @Override
    PageRequest newInstance(long pageNumber, String identifier) {
        return instance;
    }

    @Override
    int size() {
        return 0;
    }

    @Override
    long[] getValues() {
        return new long[0];
    }

    @Override
    void setValues(long[] values) {
        // nothing to do
    }

    @Override
    void setDefaultValues() {
        // nothing to do
    }

    @Override
    public String getOperationName() {
        return "";
    }

    @Override
    PageRequest generateRequestFor(long pageNumber) {
        return instance;
    }

    @Override
    String getIdentifier() {
        return "";
    }

    @Override
    public long getPageNumber() {
        return 0;
    }

    @Override
    public String first(HttpServletRequest request) {
        return null;
    }

    @Override
    public String next(HttpServletRequest request) {
        return null;
    }

    @Override
    public String previous(HttpServletRequest request) {
        return null;
    }

    @Override
    void cacheValues() throws IOException {
        // nothing to do
    }

    @Override
    public void updateValues() throws IOException {
        // nothing to do
    }
}
