//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.13 um 12:48:52 PM CET 
//


package net.opengis.fes._2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import net.opengis.ows._1.MetadataType;


/**
 * <p>Java-Klasse für AvailableFunctionType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AvailableFunctionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Metadata" minOccurs="0"/&gt;
 *         &lt;element name="Returns" type="{http://www.w3.org/2001/XMLSchema}QName"/&gt;
 *         &lt;element name="Arguments" type="{http://www.opengis.net/fes/2.0}ArgumentsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AvailableFunctionType", propOrder = {
    "metadata",
    "returns",
    "arguments"
})
public class AvailableFunctionType {

    @XmlElement(name = "Metadata", namespace = "http://www.opengis.net/ows/1.1")
    protected MetadataType metadata;
    @XmlElement(name = "Returns", required = true)
    protected QName returns;
    @XmlElement(name = "Arguments")
    protected ArgumentsType arguments;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Ruft den Wert der metadata-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetadataType }
     *     
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Legt den Wert der metadata-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetadataType }
     *     
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    public boolean isSetMetadata() {
        return (this.metadata!= null);
    }

    /**
     * Ruft den Wert der returns-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getReturns() {
        return returns;
    }

    /**
     * Legt den Wert der returns-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setReturns(QName value) {
        this.returns = value;
    }

    public boolean isSetReturns() {
        return (this.returns!= null);
    }

    /**
     * Ruft den Wert der arguments-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArgumentsType }
     *     
     */
    public ArgumentsType getArguments() {
        return arguments;
    }

    /**
     * Legt den Wert der arguments-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArgumentsType }
     *     
     */
    public void setArguments(ArgumentsType value) {
        this.arguments = value;
    }

    public boolean isSetArguments() {
        return (this.arguments!= null);
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
    }

}
