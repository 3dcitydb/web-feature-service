package vcs.citydb.wfs.config.feature;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.util.Objects;

@XmlType(name = "ADEFeatureTypeType", propOrder = {
        "name",
        "titles",
        "abstracts",
        "keywords",
        "wgs84BoundingBoxes",
        "metadataURLs",
        "extendedDescription"
})
public class ADEFeatureType extends FeatureType {
    @XmlElement(required = true)
    private ADEFeatureTypeName name;

    protected QName getQName() {
        return name != null ? new QName(name.getNamespaceURI(), name.getLocalPart()) : null;
    }

    @Override
    public int hashCode() {
        return name != null ? Objects.hash(name.getLocalPart(), name.getNamespaceURI()) : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ADEFeatureType) {
            ADEFeatureType other = (ADEFeatureType) obj;
            if (name != null && other.name != null)
                return name.getLocalPart().equals(other.name.getLocalPart())
                        && name.getNamespaceURI().equals(other.name.getNamespaceURI());
        }

        return super.equals(obj);
    }

}
