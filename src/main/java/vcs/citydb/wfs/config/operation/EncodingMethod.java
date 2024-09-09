package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EncodingMethodType")
@XmlEnum
public enum EncodingMethod {
    @XmlEnumValue("KVP")
    KVP,
    @XmlEnumValue("XML")
    XML,
    @XmlEnumValue("KVP+XML")
    KVP_XML
}
