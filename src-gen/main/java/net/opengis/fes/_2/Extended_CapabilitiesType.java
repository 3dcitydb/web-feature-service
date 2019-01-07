//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.01.07 um 11:42:07 AM CET 
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
 * &lt;complexType name="Extended_CapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AdditionalOperators" type="{http://www.opengis.net/fes/2.0}AdditionalOperatorsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
