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
import net.opengis.fes._2.Filter_Capabilities;
import net.opengis.ows._1.CapabilitiesBaseType;
import org.w3._1999.xlink.ActuateType;
import org.w3._1999.xlink.ShowType;
import org.w3._1999.xlink.TypeType;


/**
 * <p>Java-Klasse für WFS_CapabilitiesType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="WFS_CapabilitiesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}CapabilitiesBaseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="WSDL" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleAttrs"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.opengis.net/wfs/2.0}FeatureTypeList" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/fes/2.0}Filter_Capabilities" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WFS_CapabilitiesType", propOrder = {
    "wsdl",
    "featureTypeList",
    "filter_Capabilities"
})
public class WFS_CapabilitiesType
    extends CapabilitiesBaseType
{

    @XmlElement(name = "WSDL")
    protected WFS_CapabilitiesType.WSDL wsdl;
    @XmlElement(name = "FeatureTypeList")
    protected FeatureTypeListType featureTypeList;
    @XmlElement(name = "Filter_Capabilities", namespace = "http://www.opengis.net/fes/2.0")
    protected Filter_Capabilities filter_Capabilities;

    /**
     * Ruft den Wert der wsdl-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link WFS_CapabilitiesType.WSDL }
     *     
     */
    public WFS_CapabilitiesType.WSDL getWSDL() {
        return wsdl;
    }

    /**
     * Legt den Wert der wsdl-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link WFS_CapabilitiesType.WSDL }
     *     
     */
    public void setWSDL(WFS_CapabilitiesType.WSDL value) {
        this.wsdl = value;
    }

    public boolean isSetWSDL() {
        return (this.wsdl!= null);
    }

    /**
     * Ruft den Wert der featureTypeList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FeatureTypeListType }
     *     
     */
    public FeatureTypeListType getFeatureTypeList() {
        return featureTypeList;
    }

    /**
     * Legt den Wert der featureTypeList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FeatureTypeListType }
     *     
     */
    public void setFeatureTypeList(FeatureTypeListType value) {
        this.featureTypeList = value;
    }

    public boolean isSetFeatureTypeList() {
        return (this.featureTypeList!= null);
    }

    /**
     * Ruft den Wert der filter_Capabilities-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Filter_Capabilities }
     *     
     */
    public Filter_Capabilities getFilter_Capabilities() {
        return filter_Capabilities;
    }

    /**
     * Legt den Wert der filter_Capabilities-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Filter_Capabilities }
     *     
     */
    public void setFilter_Capabilities(Filter_Capabilities value) {
        this.filter_Capabilities = value;
    }

    public boolean isSetFilter_Capabilities() {
        return (this.filter_Capabilities!= null);
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
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleAttrs"/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class WSDL {

        @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
        protected TypeType type;
        @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
        protected String href;
        @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
        protected String role;
        @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
        protected String arcrole;
        @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
        protected String title;
        @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
        protected ShowType show;
        @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
        protected ActuateType actuate;

        /**
         * Ruft den Wert der type-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link TypeType }
         *     
         */
        public TypeType getType() {
            if (type == null) {
                return TypeType.SIMPLE;
            } else {
                return type;
            }
        }

        /**
         * Legt den Wert der type-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link TypeType }
         *     
         */
        public void setType(TypeType value) {
            this.type = value;
        }

        public boolean isSetType() {
            return (this.type!= null);
        }

        /**
         * Ruft den Wert der href-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHref() {
            return href;
        }

        /**
         * Legt den Wert der href-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHref(String value) {
            this.href = value;
        }

        public boolean isSetHref() {
            return (this.href!= null);
        }

        /**
         * Ruft den Wert der role-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRole() {
            return role;
        }

        /**
         * Legt den Wert der role-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRole(String value) {
            this.role = value;
        }

        public boolean isSetRole() {
            return (this.role!= null);
        }

        /**
         * Ruft den Wert der arcrole-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getArcrole() {
            return arcrole;
        }

        /**
         * Legt den Wert der arcrole-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setArcrole(String value) {
            this.arcrole = value;
        }

        public boolean isSetArcrole() {
            return (this.arcrole!= null);
        }

        /**
         * Ruft den Wert der title-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTitle() {
            return title;
        }

        /**
         * Legt den Wert der title-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTitle(String value) {
            this.title = value;
        }

        public boolean isSetTitle() {
            return (this.title!= null);
        }

        /**
         * Ruft den Wert der show-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ShowType }
         *     
         */
        public ShowType getShow() {
            return show;
        }

        /**
         * Legt den Wert der show-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ShowType }
         *     
         */
        public void setShow(ShowType value) {
            this.show = value;
        }

        public boolean isSetShow() {
            return (this.show!= null);
        }

        /**
         * Ruft den Wert der actuate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ActuateType }
         *     
         */
        public ActuateType getActuate() {
            return actuate;
        }

        /**
         * Legt den Wert der actuate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ActuateType }
         *     
         */
        public void setActuate(ActuateType value) {
            this.actuate = value;
        }

        public boolean isSetActuate() {
            return (this.actuate!= null);
        }

    }

}
