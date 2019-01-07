//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.01.07 um 11:42:07 AM CET 
//


package net.opengis.ows._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * XML encoded GetCapabilities operation request. This operation allows clients to retrieve service metadata about a specific service instance. In this XML encoding, no "request" parameter is included, since the element name specifies the specific operation. This base type shall be extended by each specific OWS to include the additional required "service" attribute, with the correct value for that OWS. 
 * 
 * <p>Java-Klasse für GetCapabilitiesType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="GetCapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AcceptVersions" type="{http://www.opengis.net/ows/1.1}AcceptVersionsType" minOccurs="0"/&gt;
 *         &lt;element name="Sections" type="{http://www.opengis.net/ows/1.1}SectionsType" minOccurs="0"/&gt;
 *         &lt;element name="AcceptFormats" type="{http://www.opengis.net/ows/1.1}AcceptFormatsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="updateSequence" type="{http://www.opengis.net/ows/1.1}UpdateSequenceType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCapabilitiesType", propOrder = {
    "acceptVersions",
    "sections",
    "acceptFormats"
})
@XmlSeeAlso({
    net.opengis.wfs._2.GetCapabilitiesType.class
})
public class GetCapabilitiesType {

    @XmlElement(name = "AcceptVersions")
    protected AcceptVersionsType acceptVersions;
    @XmlElement(name = "Sections")
    protected SectionsType sections;
    @XmlElement(name = "AcceptFormats")
    protected AcceptFormatsType acceptFormats;
    @XmlAttribute(name = "updateSequence")
    protected String updateSequence;

    /**
     * Ruft den Wert der acceptVersions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AcceptVersionsType }
     *     
     */
    public AcceptVersionsType getAcceptVersions() {
        return acceptVersions;
    }

    /**
     * Legt den Wert der acceptVersions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AcceptVersionsType }
     *     
     */
    public void setAcceptVersions(AcceptVersionsType value) {
        this.acceptVersions = value;
    }

    public boolean isSetAcceptVersions() {
        return (this.acceptVersions!= null);
    }

    /**
     * Ruft den Wert der sections-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SectionsType }
     *     
     */
    public SectionsType getSections() {
        return sections;
    }

    /**
     * Legt den Wert der sections-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SectionsType }
     *     
     */
    public void setSections(SectionsType value) {
        this.sections = value;
    }

    public boolean isSetSections() {
        return (this.sections!= null);
    }

    /**
     * Ruft den Wert der acceptFormats-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AcceptFormatsType }
     *     
     */
    public AcceptFormatsType getAcceptFormats() {
        return acceptFormats;
    }

    /**
     * Legt den Wert der acceptFormats-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AcceptFormatsType }
     *     
     */
    public void setAcceptFormats(AcceptFormatsType value) {
        this.acceptFormats = value;
    }

    public boolean isSetAcceptFormats() {
        return (this.acceptFormats!= null);
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
