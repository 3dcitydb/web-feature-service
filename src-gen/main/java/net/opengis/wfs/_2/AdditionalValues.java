//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
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
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}ValueCollection"/&gt;
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}SimpleFeatureCollection"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
     *     {@link JAXBElement }{@code <}{@link FeatureCollectionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleFeatureCollectionType }{@code >}
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
     *     {@link JAXBElement }{@code <}{@link FeatureCollectionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SimpleFeatureCollectionType }{@code >}
     *     
     */
    public void setSimpleFeatureCollection(JAXBElement<? extends SimpleFeatureCollectionType> value) {
        this.simpleFeatureCollection = value;
    }

    public boolean isSetSimpleFeatureCollection() {
        return (this.simpleFeatureCollection!= null);
    }

}
