//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.fes._2;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PropertyIsBetweenType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PropertyIsBetweenType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/fes/2.0}ComparisonOpsType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/fes/2.0}expression"/&gt;
 *         &lt;element name="LowerBoundary" type="{http://www.opengis.net/fes/2.0}LowerBoundaryType"/&gt;
 *         &lt;element name="UpperBoundary" type="{http://www.opengis.net/fes/2.0}UpperBoundaryType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyIsBetweenType", propOrder = {
    "expression",
    "lowerBoundary",
    "upperBoundary"
})
public class PropertyIsBetweenType
    extends ComparisonOpsType
{

    @XmlElementRef(name = "expression", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    protected JAXBElement<?> expression;
    @XmlElement(name = "LowerBoundary", required = true)
    protected LowerBoundaryType lowerBoundary;
    @XmlElement(name = "UpperBoundary", required = true)
    protected UpperBoundaryType upperBoundary;

    /**
     * Ruft den Wert der expression-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link LiteralType }{@code >}
     *     {@link JAXBElement }{@code <}{@link FunctionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public JAXBElement<?> getExpression() {
        return expression;
    }

    /**
     * Legt den Wert der expression-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link LiteralType }{@code >}
     *     {@link JAXBElement }{@code <}{@link FunctionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public void setExpression(JAXBElement<?> value) {
        this.expression = value;
    }

    public boolean isSetExpression() {
        return (this.expression!= null);
    }

    /**
     * Ruft den Wert der lowerBoundary-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LowerBoundaryType }
     *     
     */
    public LowerBoundaryType getLowerBoundary() {
        return lowerBoundary;
    }

    /**
     * Legt den Wert der lowerBoundary-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LowerBoundaryType }
     *     
     */
    public void setLowerBoundary(LowerBoundaryType value) {
        this.lowerBoundary = value;
    }

    public boolean isSetLowerBoundary() {
        return (this.lowerBoundary!= null);
    }

    /**
     * Ruft den Wert der upperBoundary-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UpperBoundaryType }
     *     
     */
    public UpperBoundaryType getUpperBoundary() {
        return upperBoundary;
    }

    /**
     * Legt den Wert der upperBoundary-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UpperBoundaryType }
     *     
     */
    public void setUpperBoundary(UpperBoundaryType value) {
        this.upperBoundary = value;
    }

    public boolean isSetUpperBoundary() {
        return (this.upperBoundary!= null);
    }

}
