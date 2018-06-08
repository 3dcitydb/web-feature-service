package vcs.citydb.wfs.kvp.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.xml.NamespaceFilter;

public class NamespacesParser extends ValueParser<NamespaceFilter> {
	private final WFSConfig wfsConfig;

	private final String xmlns = "xmlns\\((?:([^,]+?),)?([^,\\)]+)\\)";
	private final Pattern xmlnsTest = Pattern.compile(xmlns);
	private final Pattern xmlnsListTest = Pattern.compile(xmlns + "(?:\\s*,\\s*" + xmlns + ")*");

	public NamespacesParser(WFSConfig wfsConfig) {
		this.wfsConfig = wfsConfig;
	}

	@Override
	public NamespaceFilter parse(String key, String value) throws KVPParseException {
		NamespaceFilter namespaceFilter = new NamespaceFilter();

		try {
			// set default namespace declarations
			for (Module module : Modules.getModules()) {
				String prefix = module.getNamespacePrefix();
				String namespaceURI = module.getNamespaceURI();

				if (module instanceof CityGMLModule) {
					CityGMLVersion moduleVersion = CityGMLVersion.fromCityGMLModule((CityGMLModule)module);
					if (moduleVersion == wfsConfig.getFeatureTypes().getDefaultVersion())
						namespaceFilter.startPrefixMapping(prefix, namespaceURI);

					prefix += (moduleVersion == CityGMLVersion.v2_0_0) ? "2" : "1";
				}

				namespaceFilter.startPrefixMapping(prefix, namespaceURI);
			}
			
			// add WFS namespace declarations
			namespaceFilter.startPrefixMapping(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			namespaceFilter.startPrefixMapping(Constants.FES_NAMESPACE_PREFIX, Constants.FES_NAMESPACE_URI);
			namespaceFilter.startPrefixMapping(Constants.OWS_NAMESPACE_PREFIX, Constants.OWS_NAMESPACE_URI);
			
		} catch (SAXException e) {
			//
		}

		// evaluate namespaces provided as parameter
		if (value != null) {
			try {
				Matcher matcher = xmlnsListTest.matcher(value);
				if (matcher.matches()) {
					matcher.reset().usePattern(xmlnsTest);
					while (matcher.find()) {
						String prefix = matcher.group(1);
						String namespaceURI = matcher.group(2);

						prefix = prefix != null ? prefix.trim() : XMLConstants.DEFAULT_NS_PREFIX;
						namespaceURI = namespaceURI != null ? namespaceURI.trim() : "";
						if (namespaceURI.isEmpty())
							throw new KVPParseException("The " + key + " parameter must not contain an empty namespace URI.");

						namespaceFilter.startPrefixMapping(prefix, namespaceURI);
					}
				} else
					throw new KVPParseException("The parameter " + key + " must be given as comma-separated list of one or more namespaces of the form \"xmlns(prefix,escaped_url)\".");
			} catch (SAXException e) {
				throw new KVPParseException("Failed to parse the parameter " + key + ".", e);
			}
		}

		return namespaceFilter;
	}

}
