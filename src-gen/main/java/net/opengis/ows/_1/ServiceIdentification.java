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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}DescriptionType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ServiceType" type="{http://www.opengis.net/ows/1.1}CodeType"/&gt;
 *         &lt;element name="ServiceTypeVersion" type="{http://www.opengis.net/ows/1.1}VersionType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Profile" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Fees" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}AccessConstraints" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "serviceType",
    "serviceTypeVersion",
    "profile",
    "fees",
    "accessConstraints"
})
@XmlRootElement(name = "ServiceIdentification")
public class ServiceIdentification
    extends DescriptionType
{

    @XmlElement(name = "ServiceType", required = true)
    protected CodeType serviceType;
    @XmlElement(name = "ServiceTypeVersion", required = true)
    protected List<String> serviceTypeVersion;
    @XmlElement(name = "Profile")
    @XmlSchemaType(name = "anyURI")
    protected List<String> profile;
    @XmlElement(name = "Fees")
    protected String fees;
    @XmlElement(name = "AccessConstraints")
    protected List<String> accessConstraints;

    /**
     * Ruft den Wert der serviceType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getServiceType() {
        return serviceType;
    }

    /**
     * Legt den Wert der serviceType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setServiceType(CodeType value) {
        this.serviceType = value;
    }

    public boolean isSetServiceType() {
        return (this.serviceType!= null);
    }

    /**
     * Gets the value of the serviceTypeVersion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceTypeVersion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceTypeVersion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getServiceTypeVersion() {
        if (serviceTypeVersion == null) {
            serviceTypeVersion = new ArrayList<String>();
        }
        return this.serviceTypeVersion;
    }

    public boolean isSetServiceTypeVersion() {
        return ((this.serviceTypeVersion!= null)&&(!this.serviceTypeVersion.isEmpty()));
    }

    public void unsetServiceTypeVersion() {
        this.serviceTypeVersion = null;
    }

    /**
     * Gets the value of the profile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the profile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProfile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getProfile() {
        if (profile == null) {
            profile = new ArrayList<String>();
        }
        return this.profile;
    }

    public boolean isSetProfile() {
        return ((this.profile!= null)&&(!this.profile.isEmpty()));
    }

    public void unsetProfile() {
        this.profile = null;
    }

    /**
     * If this element is omitted, no meaning is implied. 
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFees() {
        return fees;
    }

    /**
     * Legt den Wert der fees-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFees(String value) {
        this.fees = value;
    }

    public boolean isSetFees() {
        return (this.fees!= null);
    }

    /**
     * Unordered list of access constraints applied to assure the protection of privacy or intellectual property, and any other restrictions on retrieving or using data from or otherwise using this server. The reserved value NONE (case insensitive) shall be used to mean no access constraints are imposed. When this element is omitted, no meaning is implied. Gets the value of the accessConstraints property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accessConstraints property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccessConstraints().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAccessConstraints() {
        if (accessConstraints == null) {
            accessConstraints = new ArrayList<String>();
        }
        return this.accessConstraints;
    }

    public boolean isSetAccessConstraints() {
        return ((this.accessConstraints!= null)&&(!this.accessConstraints.isEmpty()));
    }

    public void unsetAccessConstraints() {
        this.accessConstraints = null;
    }

    public void setServiceTypeVersion(List<String> value) {
        this.serviceTypeVersion = value;
    }

    public void setProfile(List<String> value) {
        this.profile = value;
    }

    public void setAccessConstraints(List<String> value) {
        this.accessConstraints = value;
    }

}
