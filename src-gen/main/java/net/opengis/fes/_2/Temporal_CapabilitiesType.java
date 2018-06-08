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
 * <p>Java-Klasse für Temporal_CapabilitiesType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Temporal_CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TemporalOperands" type="{http://www.opengis.net/fes/2.0}TemporalOperandsType"/>
 *         &lt;element name="TemporalOperators" type="{http://www.opengis.net/fes/2.0}TemporalOperatorsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Temporal_CapabilitiesType", propOrder = {
    "temporalOperands",
    "temporalOperators"
})
public class Temporal_CapabilitiesType {

    @XmlElement(name = "TemporalOperands", required = true)
    protected TemporalOperandsType temporalOperands;
    @XmlElement(name = "TemporalOperators", required = true)
    protected TemporalOperatorsType temporalOperators;

    /**
     * Ruft den Wert der temporalOperands-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TemporalOperandsType }
     *     
     */
    public TemporalOperandsType getTemporalOperands() {
        return temporalOperands;
    }

    /**
     * Legt den Wert der temporalOperands-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TemporalOperandsType }
     *     
     */
    public void setTemporalOperands(TemporalOperandsType value) {
        this.temporalOperands = value;
    }

    public boolean isSetTemporalOperands() {
        return (this.temporalOperands!= null);
    }

    /**
     * Ruft den Wert der temporalOperators-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TemporalOperatorsType }
     *     
     */
    public TemporalOperatorsType getTemporalOperators() {
        return temporalOperators;
    }

    /**
     * Legt den Wert der temporalOperators-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TemporalOperatorsType }
     *     
     */
    public void setTemporalOperators(TemporalOperatorsType value) {
        this.temporalOperators = value;
    }

    public boolean isSetTemporalOperators() {
        return (this.temporalOperators!= null);
    }

}
