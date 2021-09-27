package vcs.citydb.wfs.kvp;

import org.citydb.core.registry.ObjectRegistry;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.paging.PageRequest;
import vcs.citydb.wfs.paging.PagingCacheManager;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import java.util.Map;

public class PagingReader extends KVPRequestReader {
    private final PagingCacheManager pagingCacheManager;
    private String operationName;

    public PagingReader(Map<String, String> parameters, WFSConfig wfsConfig) {
        super(parameters, wfsConfig);
        pagingCacheManager = ObjectRegistry.getInstance().lookup(PagingCacheManager.class);
    }

    @Override
    public PageRequest readRequest() throws WFSException {
        String pageId = parameters.get(KVPConstants.PAGE_ID.toUpperCase());
        if (pageId == null)
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "A paging request requires a '" + KVPConstants.PAGE_ID + "' parameter value.");

        if (!pagingCacheManager.isValidPageId(pageId))
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid value provided for the '" + KVPConstants.PAGE_ID + "' parameter.", KVPConstants.PAGE_ID);

        PageRequest pageRequest = pagingCacheManager.getIfValid(pageId);
        if (pageRequest == null)
            throw new WFSException(WFSExceptionCode.RESPONSE_CACHE_EXPIRED, "Paging results are no longer available.", pageId);

        operationName = pageRequest.getOperationName();
        return pageRequest;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public NamespaceFilter getNamespaces() throws WFSException {
        return null;
    }
}
