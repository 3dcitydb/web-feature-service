//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.ows._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Unordered list of one or more groups of references to remote and/or local resources. 
 * 
 * <p>Java-Klasse für ManifestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ManifestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}BasicIdentificationType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}ReferenceGroup" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManifestType", propOrder = {
    "referenceGroup"
})
public class ManifestType
    extends BasicIdentificationType
{

    @XmlElement(name = "ReferenceGroup", required = true)
    protected List<ReferenceGroupType> referenceGroup;

    /**
     * Gets the value of the referenceGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferenceGroupType }
     * 
     * 
     */
    public List<ReferenceGroupType> getReferenceGroup() {
        if (referenceGroup == null) {
            referenceGroup = new ArrayList<ReferenceGroupType>();
        }
        return this.referenceGroup;
    }

    public boolean isSetReferenceGroup() {
        return ((this.referenceGroup!= null)&&(!this.referenceGroup.isEmpty()));
    }

    public void unsetReferenceGroup() {
        this.referenceGroup = null;
    }

    public void setReferenceGroup(List<ReferenceGroupType> value) {
        this.referenceGroup = value;
    }

}
