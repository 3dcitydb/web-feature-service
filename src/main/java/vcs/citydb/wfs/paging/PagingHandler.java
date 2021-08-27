package vcs.citydb.wfs.paging;

import org.citydb.config.Config;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.util.LoggerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PagingHandler {
    private final Logger log = Logger.getInstance();
    private final CityGMLBuilder cityGMLBuilder;
    private final WFSConfig wfsConfig;
    private final Config config;

    public PagingHandler(CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig, Config config) {
        this.cityGMLBuilder = cityGMLBuilder;
        this.wfsConfig = wfsConfig;
        this.config = config;
    }

    public void doOperation(PageRequest pageRequest, HttpServletRequest request, HttpServletResponse response) throws WFSException {
        log.info(LoggerUtil.getLogMessage(request, "Accepting paging action for " + pageRequest.getOperationName() + " request."));

        try {
            pageRequest.updateValues();
        } catch (IOException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to update paging cache.", e);
        }

        if (KVPConstants.GET_FEATURE.equals(pageRequest.getOperationName())) {
            vcs.citydb.wfs.operation.getfeature.ExportController controller = new vcs.citydb.wfs.operation.getfeature.ExportController(cityGMLBuilder, wfsConfig, config);
            controller.doExport((GetFeatureRequest) pageRequest, request, response);
        } else if (KVPConstants.GET_PROPERTY_VALUE.equals(pageRequest.getOperationName())) {
            vcs.citydb.wfs.operation.getpropertyvalue.ExportController controller = new vcs.citydb.wfs.operation.getpropertyvalue.ExportController(cityGMLBuilder, wfsConfig, config);
            controller.doExport((GetPropertyValueRequest) pageRequest, request, response);
        } else
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to process page request with unknown operation '" + pageRequest.getOperationName() + "'.");

        log.info(LoggerUtil.getLogMessage(request, pageRequest.getOperationName() + " operation successfully finished."));
    }
}
