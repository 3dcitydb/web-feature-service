//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.wfs._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.opengis.fes._2.ResourceIdType;


/**
 * <p>Java-Klasse für CreatedOrModifiedFeatureType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CreatedOrModifiedFeatureType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;element ref="{http://www.opengis.net/fes/2.0}ResourceId"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="handle" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreatedOrModifiedFeatureType", propOrder = {
    "resourceId"
})
public class CreatedOrModifiedFeatureType {

    @XmlElement(name = "ResourceId", namespace = "http://www.opengis.net/fes/2.0", required = true)
    protected List<ResourceIdType> resourceId;
    @XmlAttribute(name = "handle")
    protected String handle;

    /**
     * Gets the value of the resourceId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceIdType }
     * 
     * 
     */
    public List<ResourceIdType> getResourceId() {
        if (resourceId == null) {
            resourceId = new ArrayList<ResourceIdType>();
        }
        return this.resourceId;
    }

    public boolean isSetResourceId() {
        return ((this.resourceId!= null)&&(!this.resourceId.isEmpty()));
    }

    public void unsetResourceId() {
        this.resourceId = null;
    }

    /**
     * Ruft den Wert der handle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Legt den Wert der handle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHandle(String value) {
        this.handle = value;
    }

    public boolean isSetHandle() {
        return (this.handle!= null);
    }

    public void setResourceId(List<ResourceIdType> value) {
        this.resourceId = value;
    }

}
