package vcs.citydb.wfs.operation.filter;

import net.opengis.fes._2.LiteralType;
import org.citydb.core.database.schema.mapping.SimpleType;
import org.citydb.core.query.filter.selection.expression.*;
import org.w3c.dom.Element;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

public class LiteralBuilder {

	public Object buildLiteral(LiteralType literalType, String handle) throws WFSException {
		// TODO we should additionally parse the type hint in future

		StringBuffer stringLiteral = null;

		for (Object content : literalType.getContent()) {
			if (content instanceof String) {
				if (stringLiteral == null)
					stringLiteral = new StringBuffer();

				stringLiteral.append((String)content);
			}

			else if (content instanceof JAXBElement<?>)
				return content;

			else if (content instanceof Element) {
				// we currently do not support unknown XML content
				Element element = (Element)content;
				QName name = new QName(element.getNamespaceURI(), element.getLocalName());
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Literal '" + name + "' is not supported.", handle);
			}
		}

		if (stringLiteral != null)
			return stringLiteral.toString();

		return null;
	}

	public AbstractLiteral<?> convertToSchemaLiteral(String literalValue, SimpleType targetType, String handle) throws WFSException {
		if (literalValue == null)
			return null;
			
		AbstractLiteral<?> literal = null;
		if (targetType != SimpleType.STRING)
			literalValue = literalValue.trim();

		switch (targetType) {
			case STRING:
				literal = new StringLiteral(literalValue);
				break;
			case DOUBLE:
				try {
					literal = new DoubleLiteral(Double.parseDouble(literalValue));
				} catch (NumberFormatException e) {
					//
				}
				break;
			case INTEGER:
				try {
					literal = new IntegerLiteral(Integer.parseInt(literalValue));
				} catch (NumberFormatException e) {
					//
				}
				break;
			case BOOLEAN:
				try {
					if ("true".equalsIgnoreCase(literalValue))
						literal = new BooleanLiteral(true);
					else if ("false".equalsIgnoreCase(literalValue))
						literal = new BooleanLiteral(false);
					else {
						long value = Integer.parseInt(literalValue);
						literal = new BooleanLiteral(value == 1);
					}
				} catch (Exception e) {
					//
				}
				break;
			case DATE:
				try {
					literal = new DateLiteral(DatatypeConverter.parseDateTime(literalValue));
					((DateLiteral)literal).setXMLLiteral(literalValue);
				} catch (IllegalArgumentException e) {
					//
				}
				break;
			case TIMESTAMP:
				try {
					XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(literalValue);
					literal = new TimestampLiteral(cal.toGregorianCalendar());
					((TimestampLiteral)literal).setXMLLiteral(literalValue);
					((TimestampLiteral)literal).setDate(cal.getXMLSchemaType() == DatatypeConstants.DATE);
				} catch (DatatypeConfigurationException | IllegalArgumentException e) {
					//
				}
				break;
			case CLOB:
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "CLOB columns are not supported in filter expressions.", handle);
		}

		return literal;
	}

}
