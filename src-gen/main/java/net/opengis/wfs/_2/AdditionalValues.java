//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.06.08 um 12:13:24 PM CEST 
//


package net.opengis.wfs._2;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}ValueCollection"/>
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}SimpleFeatureCollection"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "valueCollection",
    "simpleFeatureCollection"
})
@XmlRootElement(name = "additionalValues")
public class AdditionalValues {

    @XmlElement(name = "ValueCollection")
    protected ValueCollectionType valueCollection;
    @XmlElementRef(name = "SimpleFeatureCollection", namespace = "http://www.opengis.net/wfs/2.0", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends SimpleFeatureCollectionType> simpleFeatureCollection;

    /**
     * Ruft den Wert der valueCollection-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValueCollectionType }
     *     
     */
    public ValueCollectionType getValueCollection() {
        return valueCollection;
    }

    /**
     * Legt den Wert der valueCollection-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueCollectionType }
     *     
     */
    public void setValueCollection(ValueCollectionType value) {
        this.valueCollection = value;
    }

    public boolean isSetValueCollection() {
        return (this.valueCollection!= null);
    }

    /**
     * Ruft den Wert der simpleFeatureCollection-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SimpleFeatureCollectionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link FeatureCollectionType }{@code >}
     *     
     */
    public JAXBElement<? extends SimpleFeatureCollectionType> getSimpleFeatureCollection() {
        return simpleFeatureCollection;
    }

    /**
     * Legt den Wert der simpleFeatureCollection-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SimpleFeatureCollectionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link FeatureCollectionType }{@code >}
     *     
     */
    public void setSimpleFeatureCollection(JAXBElement<? extends SimpleFeatureCollectionType> value) {
        this.simpleFeatureCollection = value;
    }

    public boolean isSetSimpleFeatureCollection() {
        return (this.simpleFeatureCollection!= null);
    }

}
