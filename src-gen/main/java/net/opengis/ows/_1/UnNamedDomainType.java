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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Valid domain (or allowed set of values) of one quantity, with needed metadata but without a quantity name or identifier. 
 * 
 * <p>Java-Klasse für UnNamedDomainType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="UnNamedDomainType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://www.opengis.net/ows/1.1}PossibleValues"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}DefaultValue" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Meaning" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}DataType" minOccurs="0"/&gt;
 *         &lt;group ref="{http://www.opengis.net/ows/1.1}ValuesUnit" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Metadata" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnNamedDomainType", propOrder = {
    "allowedValues",
    "anyValue",
    "noValues",
    "valuesReference",
    "defaultValue",
    "meaning",
    "dataType",
    "uom",
    "referenceSystem",
    "metadata"
})
@XmlSeeAlso({
    DomainType.class
})
public class UnNamedDomainType {

    @XmlElement(name = "AllowedValues")
    protected AllowedValues allowedValues;
    @XmlElement(name = "AnyValue")
    protected AnyValue anyValue;
    @XmlElement(name = "NoValues")
    protected NoValues noValues;
    @XmlElement(name = "ValuesReference")
    protected ValuesReference valuesReference;
    @XmlElement(name = "DefaultValue")
    protected ValueType defaultValue;
    @XmlElement(name = "Meaning")
    protected DomainMetadataType meaning;
    @XmlElement(name = "DataType")
    protected DomainMetadataType dataType;
    @XmlElement(name = "UOM")
    protected DomainMetadataType uom;
    @XmlElement(name = "ReferenceSystem")
    protected DomainMetadataType referenceSystem;
    @XmlElement(name = "Metadata")
    protected List<MetadataType> metadata;

    /**
     * Ruft den Wert der allowedValues-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AllowedValues }
     *     
     */
    public AllowedValues getAllowedValues() {
        return allowedValues;
    }

    /**
     * Legt den Wert der allowedValues-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AllowedValues }
     *     
     */
    public void setAllowedValues(AllowedValues value) {
        this.allowedValues = value;
    }

    public boolean isSetAllowedValues() {
        return (this.allowedValues!= null);
    }

    /**
     * Ruft den Wert der anyValue-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AnyValue }
     *     
     */
    public AnyValue getAnyValue() {
        return anyValue;
    }

    /**
     * Legt den Wert der anyValue-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AnyValue }
     *     
     */
    public void setAnyValue(AnyValue value) {
        this.anyValue = value;
    }

    public boolean isSetAnyValue() {
        return (this.anyValue!= null);
    }

    /**
     * Ruft den Wert der noValues-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NoValues }
     *     
     */
    public NoValues getNoValues() {
        return noValues;
    }

    /**
     * Legt den Wert der noValues-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NoValues }
     *     
     */
    public void setNoValues(NoValues value) {
        this.noValues = value;
    }

    public boolean isSetNoValues() {
        return (this.noValues!= null);
    }

    /**
     * Ruft den Wert der valuesReference-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValuesReference }
     *     
     */
    public ValuesReference getValuesReference() {
        return valuesReference;
    }

    /**
     * Legt den Wert der valuesReference-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValuesReference }
     *     
     */
    public void setValuesReference(ValuesReference value) {
        this.valuesReference = value;
    }

    public boolean isSetValuesReference() {
        return (this.valuesReference!= null);
    }

    /**
     * Optional default value for this quantity, which should be included when this quantity has a default value. 
     * 
     * @return
     *     possible object is
     *     {@link ValueType }
     *     
     */
    public ValueType getDefaultValue() {
        return defaultValue;
    }

    /**
     * Legt den Wert der defaultValue-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueType }
     *     
     */
    public void setDefaultValue(ValueType value) {
        this.defaultValue = value;
    }

    public boolean isSetDefaultValue() {
        return (this.defaultValue!= null);
    }

    /**
     * Meaning metadata should be referenced or included for each quantity. 
     * 
     * @return
     *     possible object is
     *     {@link DomainMetadataType }
     *     
     */
    public DomainMetadataType getMeaning() {
        return meaning;
    }

    /**
     * Legt den Wert der meaning-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DomainMetadataType }
     *     
     */
    public void setMeaning(DomainMetadataType value) {
        this.meaning = value;
    }

    public boolean isSetMeaning() {
        return (this.meaning!= null);
    }

    /**
     * This data type metadata should be referenced or included for each quantity. 
     * 
     * @return
     *     possible object is
     *     {@link DomainMetadataType }
     *     
     */
    public DomainMetadataType getDataType() {
        return dataType;
    }

    /**
     * Legt den Wert der dataType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DomainMetadataType }
     *     
     */
    public void setDataType(DomainMetadataType value) {
        this.dataType = value;
    }

    public boolean isSetDataType() {
        return (this.dataType!= null);
    }

    /**
     * Identifier of unit of measure of this set of values. Should be included then this set of values has units (and not a more complete reference system). 
     * 
     * @return
     *     possible object is
     *     {@link DomainMetadataType }
     *     
     */
    public DomainMetadataType getUOM() {
        return uom;
    }

    /**
     * Legt den Wert der uom-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DomainMetadataType }
     *     
     */
    public void setUOM(DomainMetadataType value) {
        this.uom = value;
    }

    public boolean isSetUOM() {
        return (this.uom!= null);
    }

    /**
     * Identifier of reference system used by this set of values. Should be included then this set of values has a reference system (not just units). 
     * 
     * @return
     *     possible object is
     *     {@link DomainMetadataType }
     *     
     */
    public DomainMetadataType getReferenceSystem() {
        return referenceSystem;
    }

    /**
     * Legt den Wert der referenceSystem-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DomainMetadataType }
     *     
     */
    public void setReferenceSystem(DomainMetadataType value) {
        this.referenceSystem = value;
    }

    public boolean isSetReferenceSystem() {
        return (this.referenceSystem!= null);
    }

    /**
     * Optional unordered list of other metadata about this quantity. A list of required and optional other metadata elements for this quantity should be specified in the Implementation Specification for this service. Gets the value of the metadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataType }
     * 
     * 
     */
    public List<MetadataType> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<MetadataType>();
        }
        return this.metadata;
    }

    public boolean isSetMetadata() {
        return ((this.metadata!= null)&&(!this.metadata.isEmpty()));
    }

    public void unsetMetadata() {
        this.metadata = null;
    }

    public void setMetadata(List<MetadataType> value) {
        this.metadata = value;
    }

}
