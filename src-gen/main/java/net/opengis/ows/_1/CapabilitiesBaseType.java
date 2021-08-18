//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.13 um 12:48:52 PM CET 
//


package net.opengis.ows._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import net.opengis.wfs._2.WFS_CapabilitiesType;


/**
 * XML encoded GetCapabilities operation response. This document provides clients with service metadata about a specific service instance, usually including metadata about the tightly-coupled data served. If the server does not implement the updateSequence parameter, the server shall always return the complete Capabilities document, without the updateSequence parameter. When the server implements the updateSequence parameter and the GetCapabilities operation request included the updateSequence parameter with the current value, the server shall return this element with only the "version" and "updateSequence" attributes. Otherwise, all optional elements shall be included or not depending on the actual value of the Contents parameter in the GetCapabilities operation request. This base type shall be extended by each specific OWS to include the additional contents needed. 
 * 
 * <p>Java-Klasse für CapabilitiesBaseType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CapabilitiesBaseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}ServiceIdentification" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}ServiceProvider" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}OperationsMetadata" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" use="required" type="{http://www.opengis.net/ows/1.1}VersionType" /&gt;
 *       &lt;attribute name="updateSequence" type="{http://www.opengis.net/ows/1.1}UpdateSequenceType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CapabilitiesBaseType", propOrder = {
    "serviceIdentification",
    "serviceProvider",
    "operationsMetadata"
})
@XmlSeeAlso({
    WFS_CapabilitiesType.class
})
public class CapabilitiesBaseType {

    @XmlElement(name = "ServiceIdentification")
    protected ServiceIdentification serviceIdentification;
    @XmlElement(name = "ServiceProvider")
    protected ServiceProvider serviceProvider;
    @XmlElement(name = "OperationsMetadata")
    protected OperationsMetadata operationsMetadata;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "updateSequence")
    protected String updateSequence;

    /**
     * Ruft den Wert der serviceIdentification-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ServiceIdentification }
     *     
     */
    public ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * Legt den Wert der serviceIdentification-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceIdentification }
     *     
     */
    public void setServiceIdentification(ServiceIdentification value) {
        this.serviceIdentification = value;
    }

    public boolean isSetServiceIdentification() {
        return (this.serviceIdentification!= null);
    }

    /**
     * Ruft den Wert der serviceProvider-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ServiceProvider }
     *     
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * Legt den Wert der serviceProvider-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceProvider }
     *     
     */
    public void setServiceProvider(ServiceProvider value) {
        this.serviceProvider = value;
    }

    public boolean isSetServiceProvider() {
        return (this.serviceProvider!= null);
    }

    /**
     * Ruft den Wert der operationsMetadata-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OperationsMetadata }
     *     
     */
    public OperationsMetadata getOperationsMetadata() {
        return operationsMetadata;
    }

    /**
     * Legt den Wert der operationsMetadata-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationsMetadata }
     *     
     */
    public void setOperationsMetadata(OperationsMetadata value) {
        this.operationsMetadata = value;
    }

    public boolean isSetOperationsMetadata() {
        return (this.operationsMetadata!= null);
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    public boolean isSetVersion() {
        return (this.version!= null);
    }

    /**
     * Ruft den Wert der updateSequence-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * Legt den Wert der updateSequence-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdateSequence(String value) {
        this.updateSequence = value;
    }

    public boolean isSetUpdateSequence() {
        return (this.updateSequence!= null);
    }

}
