//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.11 um 09:31:43 PM CEST 
//


package net.opengis.wfs._2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.opengis.ows._1.ExceptionReport;


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
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}ExceptionReport"/&gt;
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
    "exceptionReport"
})
@XmlRootElement(name = "truncatedResponse")
public class TruncatedResponse {

    @XmlElement(name = "ExceptionReport", namespace = "http://www.opengis.net/ows/1.1", required = true)
    protected ExceptionReport exceptionReport;

    /**
     * Ruft den Wert der exceptionReport-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExceptionReport }
     *     
     */
    public ExceptionReport getExceptionReport() {
        return exceptionReport;
    }

    /**
     * Legt den Wert der exceptionReport-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExceptionReport }
     *     
     */
    public void setExceptionReport(ExceptionReport value) {
        this.exceptionReport = value;
    }

    public boolean isSetExceptionReport() {
        return (this.exceptionReport!= null);
    }

}
