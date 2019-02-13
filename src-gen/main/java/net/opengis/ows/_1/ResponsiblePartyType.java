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
import javax.xml.bind.annotation.XmlType;


/**
 * Identification of, and means of communication with, person responsible for the server. At least one of IndividualName, OrganisationName, or PositionName shall be included. 
 * 
 * <p>Java-Klasse für ResponsiblePartyType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ResponsiblePartyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}IndividualName" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}OrganisationName" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}PositionName" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}ContactInfo" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Role"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponsiblePartyType", propOrder = {
    "individualName",
    "organisationName",
    "positionName",
    "contactInfo",
    "role"
})
public class ResponsiblePartyType {

    @XmlElement(name = "IndividualName")
    protected String individualName;
    @XmlElement(name = "OrganisationName")
    protected String organisationName;
    @XmlElement(name = "PositionName")
    protected String positionName;
    @XmlElement(name = "ContactInfo")
    protected ContactType contactInfo;
    @XmlElement(name = "Role", required = true)
    protected CodeType role;

    /**
     * Ruft den Wert der individualName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndividualName() {
        return individualName;
    }

    /**
     * Legt den Wert der individualName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndividualName(String value) {
        this.individualName = value;
    }

    public boolean isSetIndividualName() {
        return (this.individualName!= null);
    }

    /**
     * Ruft den Wert der organisationName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * Legt den Wert der organisationName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationName(String value) {
        this.organisationName = value;
    }

    public boolean isSetOrganisationName() {
        return (this.organisationName!= null);
    }

    /**
     * Ruft den Wert der positionName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPositionName() {
        return positionName;
    }

    /**
     * Legt den Wert der positionName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPositionName(String value) {
        this.positionName = value;
    }

    public boolean isSetPositionName() {
        return (this.positionName!= null);
    }

    /**
     * Ruft den Wert der contactInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ContactType }
     *     
     */
    public ContactType getContactInfo() {
        return contactInfo;
    }

    /**
     * Legt den Wert der contactInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactType }
     *     
     */
    public void setContactInfo(ContactType value) {
        this.contactInfo = value;
    }

    public boolean isSetContactInfo() {
        return (this.contactInfo!= null);
    }

    /**
     * Ruft den Wert der role-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getRole() {
        return role;
    }

    /**
     * Legt den Wert der role-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setRole(CodeType value) {
        this.role = value;
    }

    public boolean isSetRole() {
        return (this.role!= null);
    }

}
