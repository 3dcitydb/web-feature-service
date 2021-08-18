package vcs.citydb.wfs.config.processing;

import org.citydb.config.project.common.XSLTransformation;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="PreProcessingType", propOrder={
        "xslTransformation"
})
public class PreProcessing {
    private XSLTransformation xslTransformation;

    public PreProcessing() {
        xslTransformation = new XSLTransformation();
    }

    public XSLTransformation getXSLTransformation() {
        return xslTransformation;
    }

    public void setXSLTransformation(XSLTransformation xslTransformation) {
        this.xslTransformation = xslTransformation;
    }
}
