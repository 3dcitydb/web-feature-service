package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.SortByType;
import net.opengis.fes._2.SortPropertyType;
import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.database.schema.util.SimpleXPathParser;
import org.citydb.core.database.schema.util.XPathException;
import org.citydb.core.query.Query;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citydb.core.query.filter.sorting.SortOrder;
import org.citydb.core.query.filter.sorting.SortProperty;
import org.citydb.core.query.filter.sorting.Sorting;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.bind.JAXBElement;
import java.util.HashSet;
import java.util.Set;

public class SortingHandler {
    private final SimpleXPathParser xpathParser;

    public SortingHandler(SimpleXPathParser xpathParser) {
        this.xpathParser = xpathParser;
    }

    public Sorting getSorting(JAXBElement<?> sortingClauseElement, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        if (!(sortingClauseElement.getValue() instanceof SortByType))
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The element '" + sortingClauseElement.getName() + "' is not supported as sorting clause of queries.", handle);

        SortByType sortBy = (SortByType) sortingClauseElement.getValue();
        if (!sortBy.isSetSortProperty())
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The SortBy element must contain at least one sort property.", handle);

        Sorting sorting = new Sorting();
        Set<String> valueReferences = new HashSet<>();

        try {
            for (SortPropertyType propertyType : sortBy.getSortProperty()) {
                if (!valueReferences.add(propertyType.getValueReference()))
                    throw new WFSException(WFSExceptionCode.DUPLICATE_SORT_KEY, "Duplicate value references pointing to the same sorting key are not allowed.", propertyType.getValueReference());

                ValueReference valueReference = parseValueReference(propertyType.getValueReference(), featureType, namespaceFilter, handle);
                if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
                    throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The value reference of a sorting property must point to a simple thematic attribute.", handle);

                SortProperty sortProperty = new SortProperty(valueReference);
                if (propertyType.isSetSortOrder()) {
                    switch (propertyType.getSortOrder()) {
                        case ASC:
                            sortProperty.setSortOrder(SortOrder.ASCENDING);
                            break;
                        case DESC:
                            sortProperty.setSortOrder(SortOrder.DESCENDING);
                            break;
                    }
                }

                sorting.addSortProperty(sortProperty);
            }
        } catch (FilterException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to build the sorting clause.", handle, e);
        }

        return sorting;
    }

    public void setDefaultSorting(Query query, FeatureType featureType, String handle) throws WFSException {
        try {
            SchemaPath schemaPath = new SchemaPath(featureType);
            schemaPath.appendChild(featureType.getProperty(MappingConstants.ID, CityDBADE200Module.v3_0.getNamespaceURI(), true));
            query.setSorting(new Sorting(new SortProperty(new ValueReference(schemaPath))));
        } catch (InvalidSchemaPathException | FilterException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to add default sorting by primary key to query.", handle);
        }
    }

    private ValueReference parseValueReference(String valueReference_, FeatureType featureType, NamespaceFilter namespaceFilter, String handle) throws WFSException {
        try {
            SchemaPath schemaPath = xpathParser.parse(valueReference_, featureType, namespaceFilter);
            return new ValueReference(schemaPath);
        } catch (XPathException e) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid XPath expression used in ValueReference.", KVPConstants.VALUE_REFERENCE, e);
        } catch (InvalidSchemaPathException e) {
            throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to map XPath expression used in ValueReference to the CityGML schema.", handle, e);
        }
    }

}
