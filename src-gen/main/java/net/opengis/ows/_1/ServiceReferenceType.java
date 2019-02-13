//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.ows._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Complete reference to a remote resource that needs to be retrieved from an OWS using an XML-encoded operation request. This element shall be used, within an InputData or Manifest element that is used for input data, when that input data needs to be retrieved from another web service using a XML-encoded OWS operation request. This element shall not be used for local payload input data or for requesting the resource from a web server using HTTP Get. 
 * 
 * <p>Java-Klasse für ServiceReferenceType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ServiceReferenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}ReferenceType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="RequestMessage" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *         &lt;element name="RequestMessageReference" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceReferenceType", propOrder = {
    "requestMessage",
    "requestMessageReference"
})
public class ServiceReferenceType
    extends ReferenceType
{

    @XmlElement(name = "RequestMessage")
    protected Object requestMessage;
    @XmlElement(name = "RequestMessageReference")
    @XmlSchemaType(name = "anyURI")
    protected String requestMessageReference;

    /**
     * Ruft den Wert der requestMessage-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getRequestMessage() {
        return requestMessage;
    }

    /**
     * Legt den Wert der requestMessage-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setRequestMessage(Object value) {
        this.requestMessage = value;
    }

    public boolean isSetRequestMessage() {
        return (this.requestMessage!= null);
    }

    /**
     * Ruft den Wert der requestMessageReference-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestMessageReference() {
        return requestMessageReference;
    }

    /**
     * Legt den Wert der requestMessageReference-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestMessageReference(String value) {
        this.requestMessageReference = value;
    }

    public boolean isSetRequestMessageReference() {
        return (this.requestMessageReference!= null);
    }

}
