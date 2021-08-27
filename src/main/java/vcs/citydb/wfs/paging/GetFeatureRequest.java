package vcs.citydb.wfs.paging;

import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ResultTypeType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.operation.getfeature.QueryExpression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class GetFeatureRequest extends PageRequest {
    private GetFeatureType wfsRequest;
    private List<QueryExpression> queryExpressions;
    private long startIndex;
    private long count;

    GetFeatureRequest() {
    }

    GetFeatureRequest(GetFeatureType wfsRequest, List<QueryExpression> queryExpressions, WFSConfig wfsConfig, PageObject pageObject) {
        super(pageObject, wfsConfig);
        this.wfsRequest = wfsRequest;
        this.queryExpressions = queryExpressions;

        startIndex = wfsRequest.isSetStartIndex() ? wfsRequest.getStartIndex().longValue() : 0;
        long countDefault = wfsConfig.getConstraints().getCountDefault();
        count = wfsRequest.isSetCount() && wfsRequest.getCount().longValue() < countDefault ? wfsRequest.getCount().longValue() : countDefault;
    }

    @Override
    PageRequest newInstance(long pageNumber, String identifier) {
        GetFeatureRequest instance = new GetFeatureRequest();
        instance.startIndex = startIndex;
        instance.count = count;

        instance.wfsRequest = new GetFeatureType();
        instance.wfsRequest.setStartIndex(BigInteger.valueOf(startIndex + count * pageNumber));
        instance.wfsRequest.setCount(BigInteger.valueOf(count));
        instance.wfsRequest.setResultType(ResultTypeType.RESULTS);
        instance.wfsRequest.setOutputFormat(wfsRequest.isSetOutputFormat() ? wfsRequest.getOutputFormat() : null);
        instance.wfsRequest.setIdentifier(identifier);

        instance.queryExpressions = new ArrayList<>(queryExpressions.size());
        for (QueryExpression queryExpression : queryExpressions)
            instance.queryExpressions.add(new QueryExpression(queryExpression));

        return instance;
    }

    @Override
    int size() {
        return queryExpressions.size();
    }

    @Override
    long[] getValues() {
        return queryExpressions.stream().mapToLong(q -> q.isSetStartId() ? q.getStartId() : -1).toArray();
    }

    @Override
    void setValues(long[] values) {
        for (int i = 0; i < values.length; i++)
            queryExpressions.get(i).setStartId(values[i]);
    }

    @Override
    void setDefaultValues() {
        queryExpressions.forEach(q -> q.setStartId(-1));
    }

    public GetFeatureType getWFSRequest() {
        return wfsRequest;
    }

    public List<QueryExpression> getQueryExpressions() {
        return queryExpressions;
    }

    @Override
    public String getOperationName() {
        return KVPConstants.GET_FEATURE;
    }
}
