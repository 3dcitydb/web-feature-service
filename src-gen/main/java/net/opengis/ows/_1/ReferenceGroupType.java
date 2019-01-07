//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.01.07 um 11:42:07 AM CET 
//


package net.opengis.ows._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * Logical group of one or more references to remote and/or local resources, allowing including metadata about that group. A Group can be used instead of a Manifest that can only contain one group. 
 * 
 * <p>Java-Klasse für ReferenceGroupType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ReferenceGroupType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}BasicIdentificationType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}AbstractReferenceBase" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceGroupType", propOrder = {
    "abstractReferenceBase"
})
public class ReferenceGroupType
    extends BasicIdentificationType
{

    @XmlElementRef(name = "AbstractReferenceBase", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractReferenceBaseType>> abstractReferenceBase;

    /**
     * Gets the value of the abstractReferenceBase property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractReferenceBase property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractReferenceBase().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ServiceReferenceType }{@code >}
     * {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractReferenceBaseType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractReferenceBaseType>> getAbstractReferenceBase() {
        if (abstractReferenceBase == null) {
            abstractReferenceBase = new ArrayList<JAXBElement<? extends AbstractReferenceBaseType>>();
        }
        return this.abstractReferenceBase;
    }

    public boolean isSetAbstractReferenceBase() {
        return ((this.abstractReferenceBase!= null)&&(!this.abstractReferenceBase.isEmpty()));
    }

    public void unsetAbstractReferenceBase() {
        this.abstractReferenceBase = null;
    }

    public void setAbstractReferenceBase(List<JAXBElement<? extends AbstractReferenceBaseType>> value) {
        this.abstractReferenceBase = value;
    }

}
