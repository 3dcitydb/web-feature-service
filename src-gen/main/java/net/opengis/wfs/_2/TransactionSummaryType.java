//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.wfs._2;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für TransactionSummaryType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TransactionSummaryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="totalInserted" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/&gt;
 *         &lt;element name="totalUpdated" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/&gt;
 *         &lt;element name="totalReplaced" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/&gt;
 *         &lt;element name="totalDeleted" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionSummaryType", propOrder = {
    "totalInserted",
    "totalUpdated",
    "totalReplaced",
    "totalDeleted"
})
public class TransactionSummaryType {

    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger totalInserted;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger totalUpdated;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger totalReplaced;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger totalDeleted;

    /**
     * Ruft den Wert der totalInserted-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalInserted() {
        return totalInserted;
    }

    /**
     * Legt den Wert der totalInserted-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalInserted(BigInteger value) {
        this.totalInserted = value;
    }

    public boolean isSetTotalInserted() {
        return (this.totalInserted!= null);
    }

    /**
     * Ruft den Wert der totalUpdated-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalUpdated() {
        return totalUpdated;
    }

    /**
     * Legt den Wert der totalUpdated-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalUpdated(BigInteger value) {
        this.totalUpdated = value;
    }

    public boolean isSetTotalUpdated() {
        return (this.totalUpdated!= null);
    }

    /**
     * Ruft den Wert der totalReplaced-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalReplaced() {
        return totalReplaced;
    }

    /**
     * Legt den Wert der totalReplaced-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalReplaced(BigInteger value) {
        this.totalReplaced = value;
    }

    public boolean isSetTotalReplaced() {
        return (this.totalReplaced!= null);
    }

    /**
     * Ruft den Wert der totalDeleted-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalDeleted() {
        return totalDeleted;
    }

    /**
     * Legt den Wert der totalDeleted-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalDeleted(BigInteger value) {
        this.totalDeleted = value;
    }

    public boolean isSetTotalDeleted() {
        return (this.totalDeleted!= null);
    }

}
