//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.01.07 um 11:42:07 AM CET 
//


package net.opengis.wfs._2;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für UpdateActionType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="UpdateActionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="replace"/&gt;
 *     &lt;enumeration value="insertBefore"/&gt;
 *     &lt;enumeration value="insertAfter"/&gt;
 *     &lt;enumeration value="remove"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "UpdateActionType")
@XmlEnum
public enum UpdateActionType {

    @XmlEnumValue("replace")
    REPLACE("replace"),
    @XmlEnumValue("insertBefore")
    INSERT_BEFORE("insertBefore"),
    @XmlEnumValue("insertAfter")
    INSERT_AFTER("insertAfter"),
    @XmlEnumValue("remove")
    REMOVE("remove");
    private final String value;

    UpdateActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UpdateActionType fromValue(String v) {
        for (UpdateActionType c: UpdateActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
