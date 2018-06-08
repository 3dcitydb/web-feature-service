//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.06.08 um 12:13:24 PM CEST 
//


package net.opengis.wfs._2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für LockFeatureResponseType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LockFeatureResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FeaturesLocked" type="{http://www.opengis.net/wfs/2.0}FeaturesLockedType" minOccurs="0"/>
 *         &lt;element name="FeaturesNotLocked" type="{http://www.opengis.net/wfs/2.0}FeaturesNotLockedType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lockId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LockFeatureResponseType", propOrder = {
    "featuresLocked",
    "featuresNotLocked"
})
public class LockFeatureResponseType {

    @XmlElement(name = "FeaturesLocked")
    protected FeaturesLockedType featuresLocked;
    @XmlElement(name = "FeaturesNotLocked")
    protected FeaturesNotLockedType featuresNotLocked;
    @XmlAttribute(name = "lockId")
    protected String lockId;

    /**
     * Ruft den Wert der featuresLocked-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FeaturesLockedType }
     *     
     */
    public FeaturesLockedType getFeaturesLocked() {
        return featuresLocked;
    }

    /**
     * Legt den Wert der featuresLocked-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FeaturesLockedType }
     *     
     */
    public void setFeaturesLocked(FeaturesLockedType value) {
        this.featuresLocked = value;
    }

    public boolean isSetFeaturesLocked() {
        return (this.featuresLocked!= null);
    }

    /**
     * Ruft den Wert der featuresNotLocked-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FeaturesNotLockedType }
     *     
     */
    public FeaturesNotLockedType getFeaturesNotLocked() {
        return featuresNotLocked;
    }

    /**
     * Legt den Wert der featuresNotLocked-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FeaturesNotLockedType }
     *     
     */
    public void setFeaturesNotLocked(FeaturesNotLockedType value) {
        this.featuresNotLocked = value;
    }

    public boolean isSetFeaturesNotLocked() {
        return (this.featuresNotLocked!= null);
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

}
