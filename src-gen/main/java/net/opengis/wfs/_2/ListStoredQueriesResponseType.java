//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.06.08 um 12:13:24 PM CEST 
//


package net.opengis.wfs._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ListStoredQueriesResponseType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ListStoredQueriesResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StoredQuery" type="{http://www.opengis.net/wfs/2.0}StoredQueryListItemType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListStoredQueriesResponseType", propOrder = {
    "storedQuery"
})
public class ListStoredQueriesResponseType {

    @XmlElement(name = "StoredQuery")
    protected List<StoredQueryListItemType> storedQuery;

    /**
     * Gets the value of the storedQuery property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the storedQuery property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStoredQuery().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StoredQueryListItemType }
     * 
     * 
     */
    public List<StoredQueryListItemType> getStoredQuery() {
        if (storedQuery == null) {
            storedQuery = new ArrayList<StoredQueryListItemType>();
        }
        return this.storedQuery;
    }

    public boolean isSetStoredQuery() {
        return ((this.storedQuery!= null)&&(!this.storedQuery.isEmpty()));
    }

    public void unsetStoredQuery() {
        this.storedQuery = null;
    }

    public void setStoredQuery(List<StoredQueryListItemType> value) {
        this.storedQuery = value;
    }

}
