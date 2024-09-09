package vcs.citydb.wfs.kvp.parser;

import net.opengis.fes._2.FilterType;
import net.opengis.wfs._2.QueryType;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.util.xml.NamespaceFilter;
import vcs.citydb.wfs.util.xml.ValidationEventHandlerImpl;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.StringReader;
import java.util.Iterator;

public class FilterParser extends ValueParser<FilterType> {
    private final NamespaceFilter namespaceFilter;
    private final Schema wfsSchema;
    private final CityGMLBuilder cityGMLBuilder;
    private final WFSConfig wfsConfig;

    public FilterParser(NamespaceFilter namespaceFilter, Schema wfsSchema, CityGMLBuilder cityGMLBuilder, WFSConfig wfsConfig) {
        this.namespaceFilter = namespaceFilter;
        this.wfsSchema = wfsSchema;
        this.cityGMLBuilder = cityGMLBuilder;
        this.wfsConfig = wfsConfig;
    }

    @Override
    public FilterType parse(String key, String value) throws KVPParseException {
        try {
            String queryExpression = getQueryExpressions(value.trim());

            Unmarshaller unmarshaller = cityGMLBuilder.getJAXBContext().createUnmarshaller();

            // support XML validation
            if (wfsConfig.getOperations().getRequestEncoding().isUseXMLValidation()) {
                unmarshaller.setSchema(wfsSchema);
                unmarshaller.setEventHandler(new ValidationEventHandlerImpl());
            }

            Object result = unmarshaller.unmarshal(new StringReader(queryExpression));
            if (!(result instanceof JAXBElement<?>))
                throw new KVPParseException("Failed to parse the XML encoded filter expression of parameter " + key + ".", key);

            JAXBElement<?> jaxbElement = (JAXBElement<?>) result;
            if (!(jaxbElement.getValue() instanceof QueryType))
                throw new KVPParseException("Failed to parse the XML encoded filter expression of parameter " + key + ".", key);

            QueryType query = (QueryType) jaxbElement.getValue();
            if (!query.isSetAbstractSelectionClause() || !(query.getAbstractSelectionClause().getValue() instanceof FilterType))
                throw new KVPParseException("Failed to parse the XML encoded filter expression of parameter " + key + ".", key);

            return (FilterType) query.getAbstractSelectionClause().getValue();
        } catch (JAXBException e) {
            throw new KVPParseException("Failed to parse the XML encoded filter expression of parameter " + key + ".", key, e);
        }
    }

    private String getQueryExpressions(String filterExpression) {
        StringBuilder wfsPrefix = new StringBuilder(namespaceFilter.getPrefix(Constants.WFS_NAMESPACE_URI));
        if (!wfsPrefix.toString().equals(XMLConstants.DEFAULT_NS_PREFIX))
            wfsPrefix.append(":");

        StringBuilder dummyQuery = new StringBuilder("<").append(wfsPrefix.toString()).append("Query typeNames=\"\" ");
        Iterator<String> iter = namespaceFilter.getPrefixes();
        while (iter.hasNext()) {
            String prefix = iter.next();
            if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) || prefix.equals(XMLConstants.XML_NS_PREFIX))
                continue;

            dummyQuery.append(XMLConstants.XMLNS_ATTRIBUTE);
            if (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))
                dummyQuery.append(":").append(prefix);

            dummyQuery.append("=\"").append(namespaceFilter.getNamespaceURI(prefix)).append("\" ");
        }

        dummyQuery.append(">").append(filterExpression).append("</").append(wfsPrefix.toString()).append("Query>");

        return dummyQuery.toString();
    }

}
