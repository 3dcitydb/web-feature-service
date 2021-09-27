package vcs.citydb.wfs.paging;

import net.opengis.wfs._2.GetPropertyValueType;
import net.opengis.wfs._2.ResultTypeType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.operation.getpropertyvalue.QueryExpression;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import java.math.BigInteger;

public class GetPropertyValueRequest extends PageRequest {
    private GetPropertyValueType wfsRequest;
    private QueryExpression queryExpression;
    private NamespaceFilter namespaceFilter;
    private long startIndex;
    private long count;

    GetPropertyValueRequest() {
    }

    GetPropertyValueRequest(GetPropertyValueType wfsRequest, QueryExpression queryExpression, NamespaceFilter namespaceFilter, WFSConfig wfsConfig, PageObject pageObject) {
        super(pageObject, wfsConfig);
        this.wfsRequest = wfsRequest;
        this.queryExpression = queryExpression;
        this.namespaceFilter = namespaceFilter;

        startIndex = wfsRequest.isSetStartIndex() ? wfsRequest.getStartIndex().longValue() : 0;
        long countDefault = wfsConfig.getConstraints().getCountDefault();
        count = wfsRequest.isSetCount() && wfsRequest.getCount().longValue() < countDefault ? wfsRequest.getCount().longValue() : countDefault;
    }

    @Override
    PageRequest newInstance(long pageNumber, String identifier) {
        GetPropertyValueRequest instance = new GetPropertyValueRequest();
        instance.startIndex = startIndex;
        instance.count = count;

        instance.wfsRequest = new GetPropertyValueType();
        instance.wfsRequest.setValueReference(wfsRequest.getValueReference());
        instance.wfsRequest.setStartIndex(BigInteger.valueOf(startIndex + count * pageNumber));
        instance.wfsRequest.setCount(BigInteger.valueOf(count));
        instance.wfsRequest.setResultType(ResultTypeType.RESULTS);
        instance.wfsRequest.setOutputFormat(wfsRequest.isSetOutputFormat() ? wfsRequest.getOutputFormat() : null);
        instance.wfsRequest.setIdentifier(identifier);

        instance.queryExpression = new QueryExpression(queryExpression);
        instance.namespaceFilter = namespaceFilter;

        return instance;
    }

    @Override
    int size() {
        return 2;
    }

    @Override
    long[] getValues() {
        return new long[]{queryExpression.isSetStartId() ? queryExpression.getStartId() : -1,
                queryExpression.getPropertyOffset()};
    }

    @Override
    void setValues(long[] values) {
        queryExpression.setStartId(values[0]);
        queryExpression.setPropertyOffset(values[1]);
    }

    @Override
    void setDefaultValues() {
        queryExpression.setStartId(-1);
        queryExpression.setPropertyOffset(0);
    }

    public GetPropertyValueType getWfsRequest() {
        return wfsRequest;
    }

    public QueryExpression getQueryExpression() {
        return queryExpression;
    }

    public NamespaceFilter getNamespaceFilter() {
        return namespaceFilter;
    }

    @Override
    public String getOperationName() {
        return KVPConstants.GET_PROPERTY_VALUE;
    }
}
