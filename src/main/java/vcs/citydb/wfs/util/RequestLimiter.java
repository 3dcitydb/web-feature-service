package vcs.citydb.wfs.util;

import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RequestLimiter {
    private final int timeout;
    private final ArrayBlockingQueue<String> requestQueue;

    public RequestLimiter(int maxParallelRequests, int timeout) {
        this.timeout = timeout;
        requestQueue = maxParallelRequests > 0 ? new ArrayBlockingQueue<>(maxParallelRequests, true) : null;
    }

    public RequestLimiter(WFSConfig wfsConfig) {
        this(wfsConfig.getServer().getMaxParallelRequests(), wfsConfig.getServer().getWaitTimeout());
    }

    public void requireServiceSlot(HttpServletRequest request, String locator) throws WFSException {
        if (requestQueue != null) {
            try {
                // make sure we only serve a maximum number of requests in parallel
                if (!requestQueue.offer(request.toString(), timeout, TimeUnit.SECONDS))
                    throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The service is currently unavailable because it is overloaded. " +
                            "Generally, this is a temporary state. Please retry later.", locator);
            } catch (InterruptedException e) {
                throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The service has internally interrupted the request.", locator, e);
            }
        }
    }

    public void requireServiceSlot(HttpServletRequest request) throws WFSException {
        requireServiceSlot(request, null);
    }

    public void releaseServiceSlot(HttpServletRequest request) {
        if (requestQueue != null) {
            // release slot from the request queue
            requestQueue.remove(request.toString());
        }
    }
}
