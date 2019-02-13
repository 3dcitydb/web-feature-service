//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.ows._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ProviderName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ProviderSite" type="{http://www.opengis.net/ows/1.1}OnlineResourceType" minOccurs="0"/&gt;
 *         &lt;element name="ServiceContact" type="{http://www.opengis.net/ows/1.1}ResponsiblePartySubsetType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "providerName",
    "providerSite",
    "serviceContact"
})
@XmlRootElement(name = "ServiceProvider")
public class ServiceProvider {

    @XmlElement(name = "ProviderName", required = true)
    protected String providerName;
    @XmlElement(name = "ProviderSite")
    protected OnlineResourceType providerSite;
    @XmlElement(name = "ServiceContact", required = true)
    protected ResponsiblePartySubsetType serviceContact;

    /**
     * Ruft den Wert der providerName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Legt den Wert der providerName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderName(String value) {
        this.providerName = value;
    }

    public boolean isSetProviderName() {
        return (this.providerName!= null);
    }

    /**
     * Ruft den Wert der providerSite-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OnlineResourceType }
     *     
     */
    public OnlineResourceType getProviderSite() {
        return providerSite;
    }

    /**
     * Legt den Wert der providerSite-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OnlineResourceType }
     *     
     */
    public void setProviderSite(OnlineResourceType value) {
        this.providerSite = value;
    }

    public boolean isSetProviderSite() {
        return (this.providerSite!= null);
    }

    /**
     * Ruft den Wert der serviceContact-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ResponsiblePartySubsetType }
     *     
     */
    public ResponsiblePartySubsetType getServiceContact() {
        return serviceContact;
    }

    /**
     * Legt den Wert der serviceContact-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponsiblePartySubsetType }
     *     
     */
    public void setServiceContact(ResponsiblePartySubsetType value) {
        this.serviceContact = value;
    }

    public boolean isSetServiceContact() {
        return (this.serviceContact!= null);
    }

}
