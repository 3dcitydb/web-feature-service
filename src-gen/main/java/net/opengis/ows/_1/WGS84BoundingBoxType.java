//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.02.13 um 03:40:03 PM CET 
//


package net.opengis.ows._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * This type is adapted from the general BoundingBoxType, with modified contents and documentation for use with the 2D WGS 84 coordinate reference system. 
 * 
 * <p>Java-Klasse für WGS84BoundingBoxType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="WGS84BoundingBoxType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.opengis.net/ows/1.1}BoundingBoxType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="LowerCorner" type="{http://www.opengis.net/ows/1.1}PositionType2D"/&gt;
 *         &lt;element name="UpperCorner" type="{http://www.opengis.net/ows/1.1}PositionType2D"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="crs" type="{http://www.w3.org/2001/XMLSchema}anyURI" fixed="urn:ogc:def:crs:OGC:2:84" /&gt;
 *       &lt;attribute name="dimensions" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" fixed="2" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WGS84BoundingBoxType")
public class WGS84BoundingBoxType
    extends BoundingBoxType
{


}
