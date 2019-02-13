//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.ows._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * Typical dataset metadata in typical Contents section of an OWS service metadata (Capabilities) document. This type shall be extended and/or restricted if needed for specific OWS use, to include the specific Dataset  description metadata needed. 
 * 
 * <p>Java-Klasse für DatasetDescriptionSummaryBaseType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DatasetDescriptionSummaryBaseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}DescriptionType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}WGS84BoundingBox" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Identifier" type="{http://www.opengis.net/ows/1.1}CodeType"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}BoundingBox" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Metadata" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}DatasetDescriptionSummary" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DatasetDescriptionSummaryBaseType", propOrder = {
    "wgs84BoundingBox",
    "identifier",
    "boundingBox",
    "metadata",
    "datasetDescriptionSummary"
})
public class DatasetDescriptionSummaryBaseType
    extends DescriptionType
{

    @XmlElement(name = "WGS84BoundingBox")
    protected List<WGS84BoundingBoxType> wgs84BoundingBox;
    @XmlElement(name = "Identifier", required = true)
    protected CodeType identifier;
    @XmlElementRef(name = "BoundingBox", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends BoundingBoxType>> boundingBox;
    @XmlElement(name = "Metadata")
    protected List<MetadataType> metadata;
    @XmlElement(name = "DatasetDescriptionSummary")
    protected List<DatasetDescriptionSummaryBaseType> datasetDescriptionSummary;

    /**
     * Unordered list of zero or more minimum bounding rectangles surrounding coverage data, using the WGS 84 CRS with decimal degrees and longitude before latitude. If no WGS 84 bounding box is recorded for a coverage, any such bounding boxes recorded for a higher level in a hierarchy of datasets shall apply to this coverage. If WGS 84 bounding box(es) are recorded for a coverage, any such bounding boxes recorded for a higher level in a hierarchy of datasets shall be ignored. For each lowest-level coverage in a hierarchy, at least one applicable WGS84BoundingBox shall be either recorded or inherited, to simplify searching for datasets that might overlap a specified region. If multiple WGS 84 bounding boxes are included, this shall be interpreted as the union of the areas of these bounding boxes. Gets the value of the wgs84BoundingBox property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wgs84BoundingBox property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWGS84BoundingBox().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WGS84BoundingBoxType }
     * 
     * 
     */
    public List<WGS84BoundingBoxType> getWGS84BoundingBox() {
        if (wgs84BoundingBox == null) {
            wgs84BoundingBox = new ArrayList<WGS84BoundingBoxType>();
        }
        return this.wgs84BoundingBox;
    }

    public boolean isSetWGS84BoundingBox() {
        return ((this.wgs84BoundingBox!= null)&&(!this.wgs84BoundingBox.isEmpty()));
    }

    public void unsetWGS84BoundingBox() {
        this.wgs84BoundingBox = null;
    }

    /**
     * Ruft den Wert der identifier-Eigenschaft ab.
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
     * Unordered list of zero or more minimum bounding rectangles surrounding coverage data, in AvailableCRSs.  Zero or more BoundingBoxes are  allowed in addition to one or more WGS84BoundingBoxes to allow more precise specification of the Dataset area in AvailableCRSs. These Bounding Boxes shall not use any CRS not listed as an AvailableCRS. However, an AvailableCRS can be listed without a corresponding Bounding Box. If no such bounding box is recorded for a coverage, any such bounding boxes recorded for a higher level in a hierarchy of datasets shall apply to this coverage. If such bounding box(es) are recorded for a coverage, any such bounding boxes recorded for a higher level in a hierarchy of datasets shall be ignored. If multiple bounding boxes are included with the same CRS, this shall be interpreted as the union of the areas of these bounding boxes. Gets the value of the boundingBox property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boundingBox property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoundingBox().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link WGS84BoundingBoxType }{@code >}
     * {@link JAXBElement }{@code <}{@link BoundingBoxType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends BoundingBoxType>> getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        }
        return this.boundingBox;
    }

    public boolean isSetBoundingBox() {
        return ((this.boundingBox!= null)&&(!this.boundingBox.isEmpty()));
    }

    public void unsetBoundingBox() {
        this.boundingBox = null;
    }

    /**
     * Optional unordered list of additional metadata about this dataset. A list of optional metadata elements for this dataset description could be specified in the Implementation Specification for this service. Gets the value of the metadata property.
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

    /**
     * Metadata describing zero or more unordered subsidiary datasets available from this server. Gets the value of the datasetDescriptionSummary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datasetDescriptionSummary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatasetDescriptionSummary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DatasetDescriptionSummaryBaseType }
     * 
     * 
     */
    public List<DatasetDescriptionSummaryBaseType> getDatasetDescriptionSummary() {
        if (datasetDescriptionSummary == null) {
            datasetDescriptionSummary = new ArrayList<DatasetDescriptionSummaryBaseType>();
        }
        return this.datasetDescriptionSummary;
    }

    public boolean isSetDatasetDescriptionSummary() {
        return ((this.datasetDescriptionSummary!= null)&&(!this.datasetDescriptionSummary.isEmpty()));
    }

    public void unsetDatasetDescriptionSummary() {
        this.datasetDescriptionSummary = null;
    }

    public void setWGS84BoundingBox(List<WGS84BoundingBoxType> value) {
        this.wgs84BoundingBox = value;
    }

    public void setBoundingBox(List<JAXBElement<? extends BoundingBoxType>> value) {
        this.boundingBox = value;
    }

    public void setMetadata(List<MetadataType> value) {
        this.metadata = value;
    }

    public void setDatasetDescriptionSummary(List<DatasetDescriptionSummaryBaseType> value) {
        this.datasetDescriptionSummary = value;
    }

}
