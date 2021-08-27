package vcs.citydb.wfs.config.operation;

import vcs.citydb.wfs.kvp.KVPConstants;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "WFSOperationType")
@XmlEnum
public enum WFSOperation {
    @XmlEnumValue(KVPConstants.GET_CAPABILITIES)
    GET_CAPABILITIES(KVPConstants.GET_CAPABILITIES),
    @XmlEnumValue(KVPConstants.GET_FEATURE)
    GET_FEATURE(KVPConstants.GET_FEATURE),
    @XmlEnumValue(KVPConstants.GET_PROPERTY_VALUE)
    GET_PROPERTY_VALUE(KVPConstants.GET_PROPERTY_VALUE),
    @XmlEnumValue(KVPConstants.DESCRIBE_FEATURE_TYPE)
    DESCRIBE_FEATURE_TYPE(KVPConstants.DESCRIBE_FEATURE_TYPE),
    @XmlEnumValue(KVPConstants.LIST_STORED_QUERIES)
    LIST_STORED_QUERIES(KVPConstants.LIST_STORED_QUERIES),
    @XmlEnumValue(KVPConstants.DESCRIBE_STORED_QUERIES)
    DESCRIBE_STORED_QUERIES(KVPConstants.DESCRIBE_STORED_QUERIES),
    @XmlEnumValue(KVPConstants.CREATE_STORED_QUERY)
    CREATE_STORED_QUERY(KVPConstants.CREATE_STORED_QUERY),
    @XmlEnumValue(KVPConstants.DROP_STORED_QUERY)
    DROP_STORED_QUERY(KVPConstants.DROP_STORED_QUERY);

    private final String value;

    WFSOperation(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static WFSOperation fromValue(String value) {
        for (WFSOperation c : WFSOperation.values()) {
            if (c.value.equals(value))
                return c;
        }

        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
