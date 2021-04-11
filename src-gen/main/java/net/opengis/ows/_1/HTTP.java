//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.ows._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
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
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="Get" type="{http://www.opengis.net/ows/1.1}RequestMethodType"/&gt;
 *         &lt;element name="Post" type="{http://www.opengis.net/ows/1.1}RequestMethodType"/&gt;
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
    "getOrPost"
})
@XmlRootElement(name = "HTTP")
public class HTTP {

    @XmlElementRefs({
        @XmlElementRef(name = "Get", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Post", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<RequestMethodType>> getOrPost;

    /**
     * Gets the value of the getOrPost property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the getOrPost property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGetOrPost().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link RequestMethodType }{@code >}
     * {@link JAXBElement }{@code <}{@link RequestMethodType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<RequestMethodType>> getGetOrPost() {
        if (getOrPost == null) {
            getOrPost = new ArrayList<JAXBElement<RequestMethodType>>();
        }
        return this.getOrPost;
    }

    public boolean isSetGetOrPost() {
        return ((this.getOrPost!= null)&&(!this.getOrPost.isEmpty()));
    }

    public void unsetGetOrPost() {
        this.getOrPost = null;
    }

    public void setGetOrPost(List<JAXBElement<RequestMethodType>> value) {
        this.getOrPost = value;
    }

}
