//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.06.08 um 12:13:24 PM CEST 
//


package net.opengis.wfs._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für TransactionType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TransactionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wfs/2.0}BaseRequestType">
 *       &lt;sequence>
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/wfs/2.0}AbstractTransactionAction"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="lockId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="releaseAction" type="{http://www.opengis.net/wfs/2.0}AllSomeType" default="ALL" />
 *       &lt;attribute name="srsName" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionType", propOrder = {
    "abstractTransactionAction"
})
public class TransactionType
    extends BaseRequestType
{

    @XmlElementRef(name = "AbstractTransactionAction", namespace = "http://www.opengis.net/wfs/2.0", type = JAXBElement.class, required = false)
    protected List<JAXBElement<?>> abstractTransactionAction;
    @XmlAttribute(name = "lockId")
    protected String lockId;
    @XmlAttribute(name = "releaseAction")
    protected AllSomeType releaseAction;
    @XmlAttribute(name = "srsName")
    @XmlSchemaType(name = "anyURI")
    protected String srsName;

    /**
     * Gets the value of the abstractTransactionAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractTransactionAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractTransactionAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link UpdateType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReplaceType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractTransactionActionType }{@code >}
     * {@link JAXBElement }{@code <}{@link NativeType }{@code >}
     * {@link JAXBElement }{@code <}{@link InsertType }{@code >}
     * {@link JAXBElement }{@code <}{@link DeleteType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getAbstractTransactionAction() {
        if (abstractTransactionAction == null) {
            abstractTransactionAction = new ArrayList<JAXBElement<?>>();
        }
        return this.abstractTransactionAction;
    }

    public boolean isSetAbstractTransactionAction() {
        return ((this.abstractTransactionAction!= null)&&(!this.abstractTransactionAction.isEmpty()));
    }

    public void unsetAbstractTransactionAction() {
        this.abstractTransactionAction = null;
    }

    /**
     * Ruft den Wert der lockId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLockId() {
        return lockId;
    }

    /**
     * Legt den Wert der lockId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLockId(String value) {
        this.lockId = value;
    }

    public boolean isSetLockId() {
        return (this.lockId!= null);
    }

    /**
     * Ruft den Wert der releaseAction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AllSomeType }
     *     
     */
    public AllSomeType getReleaseAction() {
        if (releaseAction == null) {
            return AllSomeType.ALL;
        } else {
            return releaseAction;
        }
    }

    /**
     * Legt den Wert der releaseAction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AllSomeType }
     *     
     */
    public void setReleaseAction(AllSomeType value) {
        this.releaseAction = value;
    }

    public boolean isSetReleaseAction() {
        return (this.releaseAction!= null);
    }

    /**
     * Ruft den Wert der srsName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * Legt den Wert der srsName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrsName(String value) {
        this.srsName = value;
    }

    public boolean isSetSrsName() {
        return (this.srsName!= null);
    }

    public void setAbstractTransactionAction(List<JAXBElement<?>> value) {
        this.abstractTransactionAction = value;
    }

}
