//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.fes._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BinaryComparisonOpType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BinaryComparisonOpType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/fes/2.0}ComparisonOpsType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/fes/2.0}expression" maxOccurs="2" minOccurs="2"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="matchCase" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="matchAction" type="{http://www.opengis.net/fes/2.0}MatchActionType" default="Any" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryComparisonOpType", propOrder = {
    "expression"
})
public class BinaryComparisonOpType
    extends ComparisonOpsType
{

    @XmlElementRef(name = "expression", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    protected List<JAXBElement<?>> expression;
    @XmlAttribute(name = "matchCase")
    protected Boolean matchCase;
    @XmlAttribute(name = "matchAction")
    protected MatchActionType matchAction;

    /**
     * Gets the value of the expression property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the expression property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExpression().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link LiteralType }{@code >}
     * {@link JAXBElement }{@code <}{@link FunctionType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getExpression() {
        if (expression == null) {
            expression = new ArrayList<JAXBElement<?>>();
        }
        return this.expression;
    }

    public boolean isSetExpression() {
        return ((this.expression!= null)&&(!this.expression.isEmpty()));
    }

    public void unsetExpression() {
        this.expression = null;
    }

    /**
     * Ruft den Wert der matchCase-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMatchCase() {
        if (matchCase == null) {
            return true;
        } else {
            return matchCase;
        }
    }

    /**
     * Legt den Wert der matchCase-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMatchCase(boolean value) {
        this.matchCase = value;
    }

    public boolean isSetMatchCase() {
        return (this.matchCase!= null);
    }

    public void unsetMatchCase() {
        this.matchCase = null;
    }

    /**
     * Ruft den Wert der matchAction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MatchActionType }
     *     
     */
    public MatchActionType getMatchAction() {
        if (matchAction == null) {
            return MatchActionType.ANY;
        } else {
            return matchAction;
        }
    }

    /**
     * Legt den Wert der matchAction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MatchActionType }
     *     
     */
    public void setMatchAction(MatchActionType value) {
        this.matchAction = value;
    }

    public boolean isSetMatchAction() {
        return (this.matchAction!= null);
    }

    public void setExpression(List<JAXBElement<?>> value) {
        this.expression = value;
    }

}
