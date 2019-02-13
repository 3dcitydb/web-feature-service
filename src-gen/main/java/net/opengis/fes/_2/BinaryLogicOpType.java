//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.fes._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BinaryLogicOpType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BinaryLogicOpType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opengis.net/fes/2.0}LogicOpsType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="2"&gt;
 *         &lt;group ref="{http://www.opengis.net/fes/2.0}FilterPredicates"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryLogicOpType", propOrder = {
    "comparisonOpsOrSpatialOpsOrTemporalOps"
})
public class BinaryLogicOpType
    extends LogicOpsType
{

    @XmlElementRefs({
        @XmlElementRef(name = "comparisonOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "spatialOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "temporalOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "logicOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "extensionOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Function", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "_Id", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> comparisonOpsOrSpatialOpsOrTemporalOps;

    /**
     * Gets the value of the comparisonOpsOrSpatialOpsOrTemporalOps property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comparisonOpsOrSpatialOpsOrTemporalOps property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComparisonOpsOrSpatialOpsOrTemporalOps().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link PropertyIsNullType }{@code >}
     * {@link JAXBElement }{@code <}{@link PropertyIsBetweenType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link PropertyIsLikeType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link PropertyIsNilType }{@code >}
     * {@link JAXBElement }{@code <}{@link ComparisonOpsType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BBOXType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link SpatialOpsType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link TemporalOpsType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link UnaryLogicOpType }{@code >}
     * {@link JAXBElement }{@code <}{@link LogicOpsType }{@code >}
     * {@link JAXBElement }{@code <}{@link ExtensionOpsType }{@code >}
     * {@link JAXBElement }{@code <}{@link FunctionType }{@code >}
     * {@link JAXBElement }{@code <}{@link ResourceIdType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractIdType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getComparisonOpsOrSpatialOpsOrTemporalOps() {
        if (comparisonOpsOrSpatialOpsOrTemporalOps == null) {
            comparisonOpsOrSpatialOpsOrTemporalOps = new ArrayList<JAXBElement<?>>();
        }
        return this.comparisonOpsOrSpatialOpsOrTemporalOps;
    }

    public boolean isSetComparisonOpsOrSpatialOpsOrTemporalOps() {
        return ((this.comparisonOpsOrSpatialOpsOrTemporalOps!= null)&&(!this.comparisonOpsOrSpatialOpsOrTemporalOps.isEmpty()));
    }

    public void unsetComparisonOpsOrSpatialOpsOrTemporalOps() {
        this.comparisonOpsOrSpatialOpsOrTemporalOps = null;
    }

    public void setComparisonOpsOrSpatialOpsOrTemporalOps(List<JAXBElement<?>> value) {
        this.comparisonOpsOrSpatialOpsOrTemporalOps = value;
    }

}
