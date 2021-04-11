//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.wfs._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import net.opengis.ows._1.KeywordsType;
import net.opengis.ows._1.WGS84BoundingBoxType;


/**
 * <p>Java-Klasse für FeatureTypeType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FeatureTypeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}QName"/&gt;
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}Title" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}Abstract" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Keywords" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="DefaultCRS" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *             &lt;element name="OtherCRS" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;/sequence&gt;
 *           &lt;element name="NoCRS"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="OutputFormats" type="{http://www.opengis.net/wfs/2.0}OutputFormatListType" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}WGS84BoundingBox" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="MetadataURL" type="{http://www.opengis.net/wfs/2.0}MetadataURLType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ExtendedDescription" type="{http://www.opengis.net/wfs/2.0}ExtendedDescriptionType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureTypeType", propOrder = {
    "name",
    "title",
    "_abstract",
    "keywords",
    "defaultCRS",
    "otherCRS",
    "noCRS",
    "outputFormats",
    "wgs84BoundingBox",
    "metadataURL",
    "extendedDescription"
})
public class FeatureTypeType {

    @XmlElement(name = "Name", required = true)
    protected QName name;
    @XmlElement(name = "Title")
    protected List<Title> title;
    @XmlElement(name = "Abstract")
    protected List<Abstract> _abstract;
    @XmlElement(name = "Keywords", namespace = "http://www.opengis.net/ows/1.1")
    protected List<KeywordsType> keywords;
    @XmlElement(name = "DefaultCRS")
    @XmlSchemaType(name = "anyURI")
    protected String defaultCRS;
    @XmlElement(name = "OtherCRS")
    @XmlSchemaType(name = "anyURI")
    protected List<String> otherCRS;
    @XmlElement(name = "NoCRS")
    protected FeatureTypeType.NoCRS noCRS;
    @XmlElement(name = "OutputFormats")
    protected OutputFormatListType outputFormats;
    @XmlElement(name = "WGS84BoundingBox", namespace = "http://www.opengis.net/ows/1.1")
    protected List<WGS84BoundingBoxType> wgs84BoundingBox;
    @XmlElement(name = "MetadataURL")
    protected List<MetadataURLType> metadataURL;
    @XmlElement(name = "ExtendedDescription")
    protected ExtendedDescriptionType extendedDescription;

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setName(QName value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
    }

    /**
     * Gets the value of the title property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the title property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTitle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Title }
     * 
     * 
     */
    public List<Title> getTitle() {
        if (title == null) {
            title = new ArrayList<Title>();
        }
        return this.title;
    }

    public boolean isSetTitle() {
        return ((this.title!= null)&&(!this.title.isEmpty()));
    }

    public void unsetTitle() {
        this.title = null;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstract property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstract().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Abstract }
     * 
     * 
     */
    public List<Abstract> getAbstract() {
        if (_abstract == null) {
            _abstract = new ArrayList<Abstract>();
        }
        return this._abstract;
    }

    public boolean isSetAbstract() {
        return ((this._abstract!= null)&&(!this._abstract.isEmpty()));
    }

    public void unsetAbstract() {
        this._abstract = null;
    }

    /**
     * Gets the value of the keywords property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keywords property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeywords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link KeywordsType }
     * 
     * 
     */
    public List<KeywordsType> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<KeywordsType>();
        }
        return this.keywords;
    }

    public boolean isSetKeywords() {
        return ((this.keywords!= null)&&(!this.keywords.isEmpty()));
    }

    public void unsetKeywords() {
        this.keywords = null;
    }

    /**
     * Ruft den Wert der defaultCRS-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultCRS() {
        return defaultCRS;
    }

    /**
     * Legt den Wert der defaultCRS-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultCRS(String value) {
        this.defaultCRS = value;
    }

    public boolean isSetDefaultCRS() {
        return (this.defaultCRS!= null);
    }

    /**
     * Gets the value of the otherCRS property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherCRS property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherCRS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOtherCRS() {
        if (otherCRS == null) {
            otherCRS = new ArrayList<String>();
        }
        return this.otherCRS;
    }

    public boolean isSetOtherCRS() {
        return ((this.otherCRS!= null)&&(!this.otherCRS.isEmpty()));
    }

    public void unsetOtherCRS() {
        this.otherCRS = null;
    }

    /**
     * Ruft den Wert der noCRS-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FeatureTypeType.NoCRS }
     *     
     */
    public FeatureTypeType.NoCRS getNoCRS() {
        return noCRS;
    }

    /**
     * Legt den Wert der noCRS-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FeatureTypeType.NoCRS }
     *     
     */
    public void setNoCRS(FeatureTypeType.NoCRS value) {
        this.noCRS = value;
    }

    public boolean isSetNoCRS() {
        return (this.noCRS!= null);
    }

    /**
     * Ruft den Wert der outputFormats-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OutputFormatListType }
     *     
     */
    public OutputFormatListType getOutputFormats() {
        return outputFormats;
    }

    /**
     * Legt den Wert der outputFormats-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OutputFormatListType }
     *     
     */
    public void setOutputFormats(OutputFormatListType value) {
        this.outputFormats = value;
    }

    public boolean isSetOutputFormats() {
        return (this.outputFormats!= null);
    }

    /**
     * Gets the value of the wgs84BoundingBox property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wgs84BoundingBox property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWGS84BoundingBox().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WGS84BoundingBoxType }
     * 
     * 
     */
    public List<WGS84BoundingBoxType> getWGS84BoundingBox() {
        if (wgs84BoundingBox == null) {
            wgs84BoundingBox = new ArrayList<WGS84BoundingBoxType>();
        }
        return this.wgs84BoundingBox;
    }

    public boolean isSetWGS84BoundingBox() {
        return ((this.wgs84BoundingBox!= null)&&(!this.wgs84BoundingBox.isEmpty()));
    }

    public void unsetWGS84BoundingBox() {
        this.wgs84BoundingBox = null;
    }

    /**
     * Gets the value of the metadataURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadataURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadataURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataURLType }
     * 
     * 
     */
    public List<MetadataURLType> getMetadataURL() {
        if (metadataURL == null) {
            metadataURL = new ArrayList<MetadataURLType>();
        }
        return this.metadataURL;
    }

    public boolean isSetMetadataURL() {
        return ((this.metadataURL!= null)&&(!this.metadataURL.isEmpty()));
    }

    public void unsetMetadataURL() {
        this.metadataURL = null;
    }

    /**
     * Ruft den Wert der extendedDescription-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExtendedDescriptionType }
     *     
     */
    public ExtendedDescriptionType getExtendedDescription() {
        return extendedDescription;
    }

    /**
     * Legt den Wert der extendedDescription-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtendedDescriptionType }
     *     
     */
    public void setExtendedDescription(ExtendedDescriptionType value) {
        this.extendedDescription = value;
    }

    public boolean isSetExtendedDescription() {
        return (this.extendedDescription!= null);
    }

    public void setTitle(List<Title> value) {
        this.title = value;
    }

    public void setAbstract(List<Abstract> value) {
        this._abstract = value;
    }

    public void setKeywords(List<KeywordsType> value) {
        this.keywords = value;
    }

    public void setOtherCRS(List<String> value) {
        this.otherCRS = value;
    }

    public void setWGS84BoundingBox(List<WGS84BoundingBoxType> value) {
        this.wgs84BoundingBox = value;
    }

    public void setMetadataURL(List<MetadataURLType> value) {
        this.metadataURL = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class NoCRS {


    }

}
