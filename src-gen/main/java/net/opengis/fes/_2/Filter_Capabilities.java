//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.fes._2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *       &lt;sequence&gt;
 *         &lt;element name="Conformance" type="{http://www.opengis.net/fes/2.0}ConformanceType"/&gt;
 *         &lt;element name="Id_Capabilities" type="{http://www.opengis.net/fes/2.0}Id_CapabilitiesType" minOccurs="0"/&gt;
 *         &lt;element name="Scalar_Capabilities" type="{http://www.opengis.net/fes/2.0}Scalar_CapabilitiesType" minOccurs="0"/&gt;
 *         &lt;element name="Spatial_Capabilities" type="{http://www.opengis.net/fes/2.0}Spatial_CapabilitiesType" minOccurs="0"/&gt;
 *         &lt;element name="Temporal_Capabilities" type="{http://www.opengis.net/fes/2.0}Temporal_CapabilitiesType" minOccurs="0"/&gt;
 *         &lt;element name="Functions" type="{http://www.opengis.net/fes/2.0}AvailableFunctionsType" minOccurs="0"/&gt;
 *         &lt;element name="Extended_Capabilities" type="{http://www.opengis.net/fes/2.0}Extended_CapabilitiesType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "conformance",
    "id_Capabilities",
    "scalar_Capabilities",
    "spatial_Capabilities",
    "temporal_Capabilities",
    "functions",
    "extended_Capabilities"
})
@XmlRootElement(name = "Filter_Capabilities")
public class Filter_Capabilities {

    @XmlElement(name = "Conformance", required = true)
    protected ConformanceType conformance;
    @XmlElement(name = "Id_Capabilities")
    protected Id_CapabilitiesType id_Capabilities;
    @XmlElement(name = "Scalar_Capabilities")
    protected Scalar_CapabilitiesType scalar_Capabilities;
    @XmlElement(name = "Spatial_Capabilities")
    protected Spatial_CapabilitiesType spatial_Capabilities;
    @XmlElement(name = "Temporal_Capabilities")
    protected Temporal_CapabilitiesType temporal_Capabilities;
    @XmlElement(name = "Functions")
    protected AvailableFunctionsType functions;
    @XmlElement(name = "Extended_Capabilities")
    protected Extended_CapabilitiesType extended_Capabilities;

    /**
     * Ruft den Wert der conformance-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConformanceType }
     *     
     */
    public ConformanceType getConformance() {
        return conformance;
    }

    /**
     * Legt den Wert der conformance-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConformanceType }
     *     
     */
    public void setConformance(ConformanceType value) {
        this.conformance = value;
    }

    public boolean isSetConformance() {
        return (this.conformance!= null);
    }

    /**
     * Ruft den Wert der id_Capabilities-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Id_CapabilitiesType }
     *     
     */
    public Id_CapabilitiesType getId_Capabilities() {
        return id_Capabilities;
    }

    /**
     * Legt den Wert der id_Capabilities-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Id_CapabilitiesType }
     *     
     */
    public void setId_Capabilities(Id_CapabilitiesType value) {
        this.id_Capabilities = value;
    }

    public boolean isSetId_Capabilities() {
        return (this.id_Capabilities!= null);
    }

    /**
     * Ruft den Wert der scalar_Capabilities-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Scalar_CapabilitiesType }
     *     
     */
    public Scalar_CapabilitiesType getScalar_Capabilities() {
        return scalar_Capabilities;
    }

    /**
     * Legt den Wert der scalar_Capabilities-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Scalar_CapabilitiesType }
     *     
     */
    public void setScalar_Capabilities(Scalar_CapabilitiesType value) {
        this.scalar_Capabilities = value;
    }

    public boolean isSetScalar_Capabilities() {
        return (this.scalar_Capabilities!= null);
    }

    /**
     * Ruft den Wert der spatial_Capabilities-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Spatial_CapabilitiesType }
     *     
     */
    public Spatial_CapabilitiesType getSpatial_Capabilities() {
        return spatial_Capabilities;
    }

    /**
     * Legt den Wert der spatial_Capabilities-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Spatial_CapabilitiesType }
     *     
     */
    public void setSpatial_Capabilities(Spatial_CapabilitiesType value) {
        this.spatial_Capabilities = value;
    }

    public boolean isSetSpatial_Capabilities() {
        return (this.spatial_Capabilities!= null);
    }

    /**
     * Ruft den Wert der temporal_Capabilities-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Temporal_CapabilitiesType }
     *     
     */
    public Temporal_CapabilitiesType getTemporal_Capabilities() {
        return temporal_Capabilities;
    }

    /**
     * Legt den Wert der temporal_Capabilities-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Temporal_CapabilitiesType }
     *     
     */
    public void setTemporal_Capabilities(Temporal_CapabilitiesType value) {
        this.temporal_Capabilities = value;
    }

    public boolean isSetTemporal_Capabilities() {
        return (this.temporal_Capabilities!= null);
    }

    /**
     * Ruft den Wert der functions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AvailableFunctionsType }
     *     
     */
    public AvailableFunctionsType getFunctions() {
        return functions;
    }

    /**
     * Legt den Wert der functions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AvailableFunctionsType }
     *     
     */
    public void setFunctions(AvailableFunctionsType value) {
        this.functions = value;
    }

    public boolean isSetFunctions() {
        return (this.functions!= null);
    }

    /**
     * Ruft den Wert der extended_Capabilities-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Extended_CapabilitiesType }
     *     
     */
    public Extended_CapabilitiesType getExtended_Capabilities() {
        return extended_Capabilities;
    }

    /**
     * Legt den Wert der extended_Capabilities-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Extended_CapabilitiesType }
     *     
     */
    public void setExtended_Capabilities(Extended_CapabilitiesType value) {
        this.extended_Capabilities = value;
    }

    public boolean isSetExtended_Capabilities() {
        return (this.extended_Capabilities!= null);
    }

}
