//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.wfs._2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java-Klasse für PropertyType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PropertyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ValueReference"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                 &lt;attribute name="action" type="{http://www.opengis.net/wfs/2.0}UpdateActionType" default="replace" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/simpleContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyType", propOrder = {
    "valueReference",
    "value"
})
public class PropertyType {

    @XmlElement(name = "ValueReference", required = true)
    protected PropertyType.ValueReference valueReference;
    @XmlElement(name = "Value")
    protected Object value;

    /**
     * Ruft den Wert der valueReference-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PropertyType.ValueReference }
     *     
     */
    public PropertyType.ValueReference getValueReference() {
        return valueReference;
    }

    /**
     * Legt den Wert der valueReference-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PropertyType.ValueReference }
     *     
     */
    public void setValueReference(PropertyType.ValueReference value) {
        this.valueReference = value;
    }

    public boolean isSetValueReference() {
        return (this.valueReference!= null);
    }

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isSetValue() {
        return (this.value!= null);
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;simpleContent&gt;
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
     *       &lt;attribute name="action" type="{http://www.opengis.net/wfs/2.0}UpdateActionType" default="replace" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/simpleContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class ValueReference {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "action")
        protected UpdateActionType action;

        /**
         * Ruft den Wert der value-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Legt den Wert der value-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        public boolean isSetValue() {
            return (this.value!= null);
        }

        /**
         * Ruft den Wert der action-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link UpdateActionType }
         *     
         */
        public UpdateActionType getAction() {
            if (action == null) {
                return UpdateActionType.REPLACE;
            } else {
                return action;
            }
        }

        /**
         * Legt den Wert der action-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link UpdateActionType }
         *     
         */
        public void setAction(UpdateActionType value) {
            this.action = value;
        }

        public boolean isSetAction() {
            return (this.action!= null);
        }

    }

}
