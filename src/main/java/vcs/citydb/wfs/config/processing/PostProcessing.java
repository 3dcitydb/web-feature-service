package vcs.citydb.wfs.config.processing;

import org.citydb.config.project.common.XSLTransformation;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="PostProcessingType", propOrder={
        "xslTransformation"
})
public class PostProcessing {
    private XSLTransformation xslTransformation;

    public PostProcessing() {
        xslTransformation = new XSLTransformation();
    }

    public XSLTransformation getXSLTransformation() {
        return xslTransformation;
    }

    public void setXSLTransformation(XSLTransformation xslTransformation) {
        this.xslTransformation = xslTransformation;
    }
}
