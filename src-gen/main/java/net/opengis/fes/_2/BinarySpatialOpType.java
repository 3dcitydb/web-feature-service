//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.13 um 12:48:52 PM CET 
//


package net.opengis.fes._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BinarySpatialOpType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BinarySpatialOpType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/fes/2.0}SpatialOpsType"&gt;
 *       &lt;choice maxOccurs="2"&gt;
 *         &lt;element ref="{http://www.opengis.net/fes/2.0}expression"/&gt;
 *         &lt;any namespace='##other'/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinarySpatialOpType", propOrder = {
    "expressionOrAny"
})
public class BinarySpatialOpType
    extends SpatialOpsType
{

    @XmlElementRef(name = "expression", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false)
    @XmlAnyElement(lax = true)
    protected List<Object> expressionOrAny;

    /**
     * Gets the value of the expressionOrAny property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the expressionOrAny property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExpressionOrAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link LiteralType }{@code >}
     * {@link JAXBElement }{@code <}{@link FunctionType }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link Object }
     * 
     * 
     */
    public List<Object> getExpressionOrAny() {
        if (expressionOrAny == null) {
            expressionOrAny = new ArrayList<Object>();
        }
        return this.expressionOrAny;
    }

    public boolean isSetExpressionOrAny() {
        return ((this.expressionOrAny!= null)&&(!this.expressionOrAny.isEmpty()));
    }

    public void unsetExpressionOrAny() {
        this.expressionOrAny = null;
    }

    public void setExpressionOrAny(List<Object> value) {
        this.expressionOrAny = value;
    }

}
