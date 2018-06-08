//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.06.08 um 12:13:24 PM CEST 
//


package net.opengis.fes._2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Extended_CapabilitiesType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Extended_CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AdditionalOperators" type="{http://www.opengis.net/fes/2.0}AdditionalOperatorsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Extended_CapabilitiesType", propOrder = {
    "additionalOperators"
})
public class Extended_CapabilitiesType {

    @XmlElement(name = "AdditionalOperators")
    protected AdditionalOperatorsType additionalOperators;

    /**
     * Ruft den Wert der additionalOperators-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AdditionalOperatorsType }
     *     
     */
    public AdditionalOperatorsType getAdditionalOperators() {
        return additionalOperators;
    }

    /**
     * Legt den Wert der additionalOperators-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AdditionalOperatorsType }
     *     
     */
    public void setAdditionalOperators(AdditionalOperatorsType value) {
        this.additionalOperators = value;
    }

    public boolean isSetAdditionalOperators() {
        return (this.additionalOperators!= null);
    }

}
