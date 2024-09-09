package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "OperationsType", propOrder = {})
public class Operations {
    private RequestEncoding requestEncoding;
    @XmlElement(name = "DescribeFeatureType")
    private DescribeFeatureTypeOperation describeFeatureType;
    @XmlElement(name = "GetPropertyValue")
    private GetPropertyValueOperation getPropertyValue;
    @XmlElement(name = "GetFeature")
    private GetFeatureOperation getFeature;
    @XmlElement(name = "ManageStoredQueries")
    private ManagedStoredQueriesOperation managedStoredQueries;

    public Operations() {
        requestEncoding = new RequestEncoding();
        describeFeatureType = new DescribeFeatureTypeOperation();
        getPropertyValue = new GetPropertyValueOperation();
        getFeature = new GetFeatureOperation();
        managedStoredQueries = new ManagedStoredQueriesOperation();
    }

    public RequestEncoding getRequestEncoding() {
        return requestEncoding;
    }

    public void setRequestEncoding(RequestEncoding requestEncoding) {
        this.requestEncoding = requestEncoding;
    }

    public DescribeFeatureTypeOperation getDescribeFeatureType() {
        return describeFeatureType;
    }

    public void setDescribeFeatureType(DescribeFeatureTypeOperation describeFeatureType) {
        this.describeFeatureType = describeFeatureType;
    }

    public GetPropertyValueOperation getGetPropertyValue() {
        return getPropertyValue;
    }

    public void setGetPropertyValue(GetPropertyValueOperation getPropertyValue) {
        this.getPropertyValue = getPropertyValue;
    }

    public GetFeatureOperation getGetFeature() {
        return getFeature;
    }

    public void setGetFeature(GetFeatureOperation getFeature) {
        this.getFeature = getFeature;
    }

    public ManagedStoredQueriesOperation getManagedStoredQueries() {
        return managedStoredQueries;
    }

    public void setManagedStoredQueries(ManagedStoredQueriesOperation managedStoredQueries) {
        this.managedStoredQueries = managedStoredQueries;
    }

}
