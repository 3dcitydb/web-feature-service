//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.13 um 12:48:52 PM CET 
//


package net.opengis.ows._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Basic metadata identifying and describing a set of data. 
 * 
 * <p>Java-Klasse für BasicIdentificationType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BasicIdentificationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}DescriptionType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Identifier" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Metadata" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BasicIdentificationType", propOrder = {
    "identifier",
    "metadata"
})
@XmlSeeAlso({
    ReferenceGroupType.class,
    ManifestType.class,
    IdentificationType.class
})
public class BasicIdentificationType
    extends DescriptionType
{

    @XmlElement(name = "Identifier")
    protected CodeType identifier;
    @XmlElement(name = "Metadata")
    protected List<MetadataType> metadata;

    /**
     * Optional unique identifier or name of this dataset. 
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getIdentifier() {
        return identifier;
    }

    /**
     * Legt den Wert der identifier-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setIdentifier(CodeType value) {
        this.identifier = value;
    }

    public boolean isSetIdentifier() {
        return (this.identifier!= null);
    }

    /**
     * Optional unordered list of additional metadata about this data(set). A list of optional metadata elements for this data identification could be specified in the Implementation Specification for this service. Gets the value of the metadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataType }
     * 
     * 
     */
    public List<MetadataType> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<MetadataType>();
        }
        return this.metadata;
    }

    public boolean isSetMetadata() {
        return ((this.metadata!= null)&&(!this.metadata.isEmpty()));
    }

    public void unsetMetadata() {
        this.metadata = null;
    }

    public void setMetadata(List<MetadataType> value) {
        this.metadata = value;
    }

}
