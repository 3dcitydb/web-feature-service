//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.13 um 12:48:52 PM CET 
//


package net.opengis.wfs._2;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für FeatureCollectionType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FeatureCollectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/wfs/2.0}SimpleFeatureCollectionType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}additionalObjects" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}truncatedResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.opengis.net/wfs/2.0}StandardResponseParameters"/&gt;
 *       &lt;attribute name="lockId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureCollectionType", propOrder = {
    "additionalObjects",
    "truncatedResponse"
})
public class FeatureCollectionType
    extends SimpleFeatureCollectionType
{

    protected AdditionalObjects additionalObjects;
    protected TruncatedResponse truncatedResponse;
    @XmlAttribute(name = "lockId")
    protected String lockId;
    @XmlAttribute(name = "timeStamp", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeStamp;
    @XmlAttribute(name = "numberMatched", required = true)
    protected String numberMatched;
    @XmlAttribute(name = "numberReturned", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger numberReturned;
    @XmlAttribute(name = "next")
    @XmlSchemaType(name = "anyURI")
    protected String next;
    @XmlAttribute(name = "previous")
    @XmlSchemaType(name = "anyURI")
    protected String previous;

    /**
     * Ruft den Wert der additionalObjects-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AdditionalObjects }
     *     
     */
    public AdditionalObjects getAdditionalObjects() {
        return additionalObjects;
    }

    /**
     * Legt den Wert der additionalObjects-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AdditionalObjects }
     *     
     */
    public void setAdditionalObjects(AdditionalObjects value) {
        this.additionalObjects = value;
    }

    public boolean isSetAdditionalObjects() {
        return (this.additionalObjects!= null);
    }

    /**
     * Ruft den Wert der truncatedResponse-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TruncatedResponse }
     *     
     */
    public TruncatedResponse getTruncatedResponse() {
        return truncatedResponse;
    }

    /**
     * Legt den Wert der truncatedResponse-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TruncatedResponse }
     *     
     */
    public void setTruncatedResponse(TruncatedResponse value) {
        this.truncatedResponse = value;
    }

    public boolean isSetTruncatedResponse() {
        return (this.truncatedResponse!= null);
    }

    /**
     * Ruft den Wert der lockId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockId() {
        return lockId;
    }

    /**
     * Legt den Wert der lockId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockId(String value) {
        this.lockId = value;
    }

    public boolean isSetLockId() {
        return (this.lockId!= null);
    }

    /**
     * Ruft den Wert der timeStamp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeStamp() {
        return timeStamp;
    }

    /**
     * Legt den Wert der timeStamp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeStamp(XMLGregorianCalendar value) {
        this.timeStamp = value;
    }

    public boolean isSetTimeStamp() {
        return (this.timeStamp!= null);
    }

    /**
     * Ruft den Wert der numberMatched-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumberMatched() {
        return numberMatched;
    }

    /**
     * Legt den Wert der numberMatched-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberMatched(String value) {
        this.numberMatched = value;
    }

    public boolean isSetNumberMatched() {
        return (this.numberMatched!= null);
    }

    /**
     * Ruft den Wert der numberReturned-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberReturned() {
        return numberReturned;
    }

    /**
     * Legt den Wert der numberReturned-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberReturned(BigInteger value) {
        this.numberReturned = value;
    }

    public boolean isSetNumberReturned() {
        return (this.numberReturned!= null);
    }

    /**
     * Ruft den Wert der next-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNext() {
        return next;
    }

    /**
     * Legt den Wert der next-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNext(String value) {
        this.next = value;
    }

    public boolean isSetNext() {
        return (this.next!= null);
    }

    /**
     * Ruft den Wert der previous-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrevious() {
        return previous;
    }

    /**
     * Legt den Wert der previous-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrevious(String value) {
        this.previous = value;
    }

    public boolean isSetPrevious() {
        return (this.previous!= null);
    }

}
