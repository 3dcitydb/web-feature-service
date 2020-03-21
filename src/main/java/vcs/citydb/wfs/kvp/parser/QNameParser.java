package vcs.citydb.wfs.kvp.parser;

import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.xml.NamespaceFilter;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

public class QNameParser extends ValueParser<QName> {
	private final NamespaceFilter namespaceFilter;

	public QNameParser(NamespaceFilter namespaceFilter) {
		this.namespaceFilter = namespaceFilter;
	}

	@Override
	public QName parse(String key, String value) throws KVPParseException {
		String[] items = value.trim().split(":");
		if (items.length > 2)
			throw new KVPParseException("The value '" + value + "' of the parameter " + key + " is not a valid qualified property name.");
		
		String prefix = null;
		String name = null;
		
		if (items.length == 1) {
			prefix = XMLConstants.DEFAULT_NS_PREFIX;
			name = items[0];
		} else {
			prefix = items[0];
			name = items[1];
		}
		
		String namespaceURI = namespaceFilter.getNamespaceURI(prefix);
		if (namespaceURI == null)
			throw new KVPParseException("The prefix '" + prefix + "' used in the property name '" + value + "' of the parameter " + key + " is not bound to a namespace URI.");
				
		return new QName(namespaceURI, name);
	}

}
