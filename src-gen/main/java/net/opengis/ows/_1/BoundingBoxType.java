//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.06.08 um 12:13:24 PM CEST 
//


package net.opengis.ows._1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * This type is adapted from the EnvelopeType of GML 3.1, with modified contents and documentation for encoding a MINIMUM size box SURROUNDING all associated data. 
 * 
 * <p>Java-Klasse für BoundingBoxType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BoundingBoxType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LowerCorner" type="{http://www.opengis.net/ows/1.1}PositionType"/>
 *         &lt;element name="UpperCorner" type="{http://www.opengis.net/ows/1.1}PositionType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="crs" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="dimensions" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoundingBoxType", propOrder = {
    "lowerCorner",
    "upperCorner"
})
@XmlSeeAlso({
    WGS84BoundingBoxType.class
})
public class BoundingBoxType {

    @XmlList
    @XmlElement(name = "LowerCorner", type = Double.class)
    protected List<Double> lowerCorner;
    @XmlList
    @XmlElement(name = "UpperCorner", type = Double.class)
    protected List<Double> upperCorner;
    @XmlAttribute(name = "crs")
    @XmlSchemaType(name = "anyURI")
    protected String crs;
    @XmlAttribute(name = "dimensions")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger dimensions;

    /**
     * Gets the value of the lowerCorner property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lowerCorner property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLowerCorner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getLowerCorner() {
        if (lowerCorner == null) {
            lowerCorner = new ArrayList<Double>();
        }
        return this.lowerCorner;
    }

    public boolean isSetLowerCorner() {
        return ((this.lowerCorner!= null)&&(!this.lowerCorner.isEmpty()));
    }

    public void unsetLowerCorner() {
        this.lowerCorner = null;
    }

    /**
     * Gets the value of the upperCorner property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the upperCorner property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUpperCorner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getUpperCorner() {
        if (upperCorner == null) {
            upperCorner = new ArrayList<Double>();
        }
        return this.upperCorner;
    }

    public boolean isSetUpperCorner() {
        return ((this.upperCorner!= null)&&(!this.upperCorner.isEmpty()));
    }

    public void unsetUpperCorner() {
        this.upperCorner = null;
    }

    /**
     * Ruft den Wert der crs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrs() {
        return crs;
    }

    /**
     * Legt den Wert der crs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrs(String value) {
        this.crs = value;
    }

    public boolean isSetCrs() {
        return (this.crs!= null);
    }

    /**
     * Ruft den Wert der dimensions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDimensions() {
        return dimensions;
    }

    /**
     * Legt den Wert der dimensions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDimensions(BigInteger value) {
        this.dimensions = value;
    }

    public boolean isSetDimensions() {
        return (this.dimensions!= null);
    }

    public void setLowerCorner(List<Double> value) {
        this.lowerCorner = value;
    }

    public void setUpperCorner(List<Double> value) {
        this.upperCorner = value;
    }

}
