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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für FilterType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FilterType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/fes/2.0}AbstractSelectionClauseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://www.opengis.net/fes/2.0}FilterPredicates"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterType", propOrder = {
    "comparisonOps",
    "spatialOps",
    "temporalOps",
    "logicOps",
    "extensionOps",
    "function",
    "_Id"
})
public class FilterType
    extends AbstractSelectionClauseType
{

    @XmlElementRef(name = "comparisonOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends ComparisonOpsType> comparisonOps;
    @XmlElementRef(name = "spatialOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends SpatialOpsType> spatialOps;
    @XmlElementRef(name = "temporalOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends TemporalOpsType> temporalOps;
    @XmlElementRef(name = "logicOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends LogicOpsType> logicOps;
    protected ExtensionOpsType extensionOps;
    @XmlElement(name = "Function")
    protected FunctionType function;
    @XmlElementRef(name = "_Id", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false)
    protected List<JAXBElement<? extends AbstractIdType>> _Id;

    /**
     * Ruft den Wert der comparisonOps-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNilType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsLikeType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsBetweenType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNullType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ComparisonOpsType }{@code >}
     *     
     */
    public JAXBElement<? extends ComparisonOpsType> getComparisonOps() {
        return comparisonOps;
    }

    /**
     * Legt den Wert der comparisonOps-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNilType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsLikeType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsBetweenType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNullType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ComparisonOpsType }{@code >}
     *     
     */
    public void setComparisonOps(JAXBElement<? extends ComparisonOpsType> value) {
        this.comparisonOps = value;
    }

    public boolean isSetComparisonOps() {
        return (this.comparisonOps!= null);
    }

    /**
     * Ruft den Wert der spatialOps-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BBOXType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SpatialOpsType }{@code >}
     *     
     */
    public JAXBElement<? extends SpatialOpsType> getSpatialOps() {
        return spatialOps;
    }

    /**
     * Legt den Wert der spatialOps-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BBOXType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SpatialOpsType }{@code >}
     *     
     */
    public void setSpatialOps(JAXBElement<? extends SpatialOpsType> value) {
        this.spatialOps = value;
    }

    public boolean isSetSpatialOps() {
        return (this.spatialOps!= null);
    }

    /**
     * Ruft den Wert der temporalOps-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TemporalOpsType }{@code >}
     *     
     */
    public JAXBElement<? extends TemporalOpsType> getTemporalOps() {
        return temporalOps;
    }

    /**
     * Legt den Wert der temporalOps-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TemporalOpsType }{@code >}
     *     
     */
    public void setTemporalOps(JAXBElement<? extends TemporalOpsType> value) {
        this.temporalOps = value;
    }

    public boolean isSetTemporalOps() {
        return (this.temporalOps!= null);
    }

    /**
     * Ruft den Wert der logicOps-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LogicOpsType }{@code >}
     *     
     */
    public JAXBElement<? extends LogicOpsType> getLogicOps() {
        return logicOps;
    }

    /**
     * Legt den Wert der logicOps-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LogicOpsType }{@code >}
     *     
     */
    public void setLogicOps(JAXBElement<? extends LogicOpsType> value) {
        this.logicOps = value;
    }

    public boolean isSetLogicOps() {
        return (this.logicOps!= null);
    }

    /**
     * Ruft den Wert der extensionOps-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionOpsType }
     *     
     */
    public ExtensionOpsType getExtensionOps() {
        return extensionOps;
    }

    /**
     * Legt den Wert der extensionOps-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionOpsType }
     *     
     */
    public void setExtensionOps(ExtensionOpsType value) {
        this.extensionOps = value;
    }

    public boolean isSetExtensionOps() {
        return (this.extensionOps!= null);
    }

    /**
     * Ruft den Wert der function-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FunctionType }
     *     
     */
    public FunctionType getFunction() {
        return function;
    }

    /**
     * Legt den Wert der function-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FunctionType }
     *     
     */
    public void setFunction(FunctionType value) {
        this.function = value;
    }

    public boolean isSetFunction() {
        return (this.function!= null);
    }

    /**
     * Gets the value of the id property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the id property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    get_Id().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ResourceIdType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractIdType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractIdType>> get_Id() {
        if (_Id == null) {
            _Id = new ArrayList<JAXBElement<? extends AbstractIdType>>();
        }
        return this._Id;
    }

    public boolean isSet_Id() {
        return ((this._Id!= null)&&(!this._Id.isEmpty()));
    }

    public void unset_Id() {
        this._Id = null;
    }

    public void set_Id(List<JAXBElement<? extends AbstractIdType>> value) {
        this._Id = value;
    }

}
