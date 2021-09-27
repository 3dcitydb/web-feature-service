package vcs.citydb.wfs.kvp.parser;

import net.opengis.wfs._2.PropertyName;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.xml.namespace.QName;

public class PropertyNameParser extends ValueParser<PropertyName> {
	private final NamespaceFilter namespaceFilter;

	public PropertyNameParser(NamespaceFilter namespaceFilter) {
		this.namespaceFilter = namespaceFilter;
	}

	@Override
	public PropertyName parse(String key, String value) throws KVPParseException {
		PropertyName propertyName = new PropertyName();

		QName name = new QNameParser(namespaceFilter).parse(key, value);
		propertyName.setValue(name);
		
		return propertyName;
	}

}
