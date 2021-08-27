//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.13 um 12:48:52 PM CET 
//


package net.opengis.wfs._2;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import net.opengis.fes._2.AbstractAdhocQueryExpressionType;
import net.opengis.fes._2.AbstractQueryExpressionType;


/**
 * <p>Java-Klasse für GetPropertyValueType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="GetPropertyValueType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/wfs/2.0}BaseRequestType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/fes/2.0}AbstractQueryExpression"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.opengis.net/wfs/2.0}StandardPresentationParameters"/&gt;
 *       &lt;attGroup ref="{http://www.opengis.net/wfs/2.0}StandardResolveParameters"/&gt;
 *       &lt;attribute name="valueReference" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="resolvePath" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetPropertyValueType", propOrder = {
    "abstractQueryExpression"
})
public class GetPropertyValueType
    extends BaseRequestType
{

    @XmlElementRef(name = "AbstractQueryExpression", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractQueryExpressionType> abstractQueryExpression;
    @XmlAttribute(name = "valueReference", required = true)
    protected String valueReference;
    @XmlAttribute(name = "resolvePath")
    protected String resolvePath;
    @XmlAttribute(name = "startIndex")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger startIndex;
    @XmlAttribute(name = "count")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger count;
    @XmlAttribute(name = "resultType")
    protected ResultTypeType resultType;
    @XmlAttribute(name = "outputFormat")
    protected String outputFormat;
    @XmlAttribute(name = "resolve")
    protected ResolveValueType resolve;
    @XmlAttribute(name = "resolveDepth")
    protected String resolveDepth;
    @XmlAttribute(name = "resolveTimeout")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger resolveTimeout;

    /**
     * Ruft den Wert der abstractQueryExpression-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link QueryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractAdhocQueryExpressionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link StoredQueryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractQueryExpressionType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractQueryExpressionType> getAbstractQueryExpression() {
        return abstractQueryExpression;
    }

    /**
     * Legt den Wert der abstractQueryExpression-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link QueryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractAdhocQueryExpressionType }{@code >}
     *     {@link JAXBElement }{@code <}{@link StoredQueryType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractQueryExpressionType }{@code >}
     *     
     */
    public void setAbstractQueryExpression(JAXBElement<? extends AbstractQueryExpressionType> value) {
        this.abstractQueryExpression = value;
    }

    public boolean isSetAbstractQueryExpression() {
        return (this.abstractQueryExpression!= null);
    }

    /**
     * Ruft den Wert der valueReference-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueReference() {
        return valueReference;
    }

    /**
     * Legt den Wert der valueReference-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueReference(String value) {
        this.valueReference = value;
    }

    public boolean isSetValueReference() {
        return (this.valueReference!= null);
    }

    /**
     * Ruft den Wert der resolvePath-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResolvePath() {
        return resolvePath;
    }

    /**
     * Legt den Wert der resolvePath-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResolvePath(String value) {
        this.resolvePath = value;
    }

    public boolean isSetResolvePath() {
        return (this.resolvePath!= null);
    }

    /**
     * Ruft den Wert der startIndex-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getStartIndex() {
        if (startIndex == null) {
            return new BigInteger("0");
        } else {
            return startIndex;
        }
    }

    /**
     * Legt den Wert der startIndex-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setStartIndex(BigInteger value) {
        this.startIndex = value;
    }

    public boolean isSetStartIndex() {
        return (this.startIndex!= null);
    }

    /**
     * Ruft den Wert der count-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCount() {
        return count;
    }

    /**
     * Legt den Wert der count-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCount(BigInteger value) {
        this.count = value;
    }

    public boolean isSetCount() {
        return (this.count!= null);
    }

    /**
     * Ruft den Wert der resultType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ResultTypeType }
     *     
     */
    public ResultTypeType getResultType() {
        if (resultType == null) {
            return ResultTypeType.RESULTS;
        } else {
            return resultType;
        }
    }

    /**
     * Legt den Wert der resultType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultTypeType }
     *     
     */
    public void setResultType(ResultTypeType value) {
        this.resultType = value;
    }

    public boolean isSetResultType() {
        return (this.resultType!= null);
    }

    /**
     * Ruft den Wert der outputFormat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputFormat() {
        if (outputFormat == null) {
            return "application/gml+xml; version=3.2";
        } else {
            return outputFormat;
        }
    }

    /**
     * Legt den Wert der outputFormat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputFormat(String value) {
        this.outputFormat = value;
    }

    public boolean isSetOutputFormat() {
        return (this.outputFormat!= null);
    }

    /**
     * Ruft den Wert der resolve-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ResolveValueType }
     *     
     */
    public ResolveValueType getResolve() {
        if (resolve == null) {
            return ResolveValueType.NONE;
        } else {
            return resolve;
        }
    }

    /**
     * Legt den Wert der resolve-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ResolveValueType }
     *     
     */
    public void setResolve(ResolveValueType value) {
        this.resolve = value;
    }

    public boolean isSetResolve() {
        return (this.resolve!= null);
    }

    /**
     * Ruft den Wert der resolveDepth-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResolveDepth() {
        if (resolveDepth == null) {
            return "*";
        } else {
            return resolveDepth;
        }
    }

    /**
     * Legt den Wert der resolveDepth-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResolveDepth(String value) {
        this.resolveDepth = value;
    }

    public boolean isSetResolveDepth() {
        return (this.resolveDepth!= null);
    }

    /**
     * Ruft den Wert der resolveTimeout-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getResolveTimeout() {
        if (resolveTimeout == null) {
            return new BigInteger("300");
        } else {
            return resolveTimeout;
        }
    }

    /**
     * Legt den Wert der resolveTimeout-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setResolveTimeout(BigInteger value) {
        this.resolveTimeout = value;
    }

    public boolean isSetResolveTimeout() {
        return (this.resolveTimeout!= null);
    }

}
