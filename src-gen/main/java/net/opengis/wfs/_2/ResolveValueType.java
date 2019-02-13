//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.wfs._2;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ResolveValueType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="ResolveValueType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="local"/&gt;
 *     &lt;enumeration value="remote"/&gt;
 *     &lt;enumeration value="all"/&gt;
 *     &lt;enumeration value="none"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ResolveValueType")
@XmlEnum
public enum ResolveValueType {

    @XmlEnumValue("local")
    LOCAL("local"),
    @XmlEnumValue("remote")
    REMOTE("remote"),
    @XmlEnumValue("all")
    ALL("all"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    ResolveValueType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResolveValueType fromValue(String v) {
        for (ResolveValueType c: ResolveValueType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
