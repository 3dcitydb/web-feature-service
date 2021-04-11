//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.ows._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Request to a service to perform the GetResourceByID operation. This operation allows a client to retrieve one or more identified resources, including datasets and resources that describe datasets or parameters. In this XML encoding, no "request" parameter is included, since the element name specifies the specific operation. 
 * 
 * <p>Java-Klasse für GetResourceByIdType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="GetResourceByIdType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ResourceID" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}OutputFormat" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="service" use="required" type="{http://www.opengis.net/ows/1.1}ServiceType" /&gt;
 *       &lt;attribute name="version" use="required" type="{http://www.opengis.net/ows/1.1}VersionType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetResourceByIdType", propOrder = {
    "resourceID",
    "outputFormat"
})
public class GetResourceByIdType {

    @XmlElement(name = "ResourceID")
    @XmlSchemaType(name = "anyURI")
    protected List<String> resourceID;
    @XmlElement(name = "OutputFormat")
    protected String outputFormat;
    @XmlAttribute(name = "service", required = true)
    protected String service;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    /**
     * Gets the value of the resourceID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getResourceID() {
        if (resourceID == null) {
            resourceID = new ArrayList<String>();
        }
        return this.resourceID;
    }

    public boolean isSetResourceID() {
        return ((this.resourceID!= null)&&(!this.resourceID.isEmpty()));
    }

    public void unsetResourceID() {
        this.resourceID = null;
    }

    /**
     * Optional reference to the data format to be used for response to this operation request. This element shall be included when multiple output formats are available for the selected resource(s), and the client desires a format other than the specified default, if any. 
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Legt den Wert der outputFormat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputFormat(String value) {
        this.outputFormat = value;
    }

    public boolean isSetOutputFormat() {
        return (this.outputFormat!= null);
    }

    /**
     * Ruft den Wert der service-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getService() {
        return service;
    }

    /**
     * Legt den Wert der service-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setService(String value) {
        this.service = value;
    }

    public boolean isSetService() {
        return (this.service!= null);
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

    public void setResourceID(List<String> value) {
        this.resourceID = value;
    }

}
