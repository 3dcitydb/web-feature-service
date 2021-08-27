package vcs.citydb.wfs.kvp.parser;

import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.kvp.KVPConstants;

import java.util.ArrayList;
import java.util.List;

public class SingleValueListParser<T> extends ValueParser<List<T>> {
	private final ValueParser<T> valueParser;

	public SingleValueListParser(ValueParser<T> valueParser) {
		this.valueParser = valueParser;
	} 

	@Override
	public List<T> parse(String key, String value) throws KVPParseException {
		String[] items = value.split(KVPConstants.LIST_DELIMITER);
		List<T> result = new ArrayList<>(items.length);

		for (int i = 0; i < items.length; i++) {
			String item = items[i];

			if (i == 0)
				item = item.replaceFirst("^\\(", "");
			if (i == items.length - 1)
				item = item.replaceFirst("\\)$", "");

			if (!item.trim().isEmpty())
				result.add(valueParser.parse(key, item));
		}

		if (result.isEmpty())
			throw new KVPParseException("The value of the parameter " + key + " must not be an empty list.", key);

		return result;
	}
}
