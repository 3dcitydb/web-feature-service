package vcs.citydb.wfs.kvp.parser;

import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.kvp.KVPConstants;

import java.util.ArrayList;
import java.util.List;

public class ValueListParser<T> extends ValueParser<List<List<T>>> {
	private final ValueParser<T> valueParser;

	public ValueListParser(ValueParser<T> valueParser) {
		this.valueParser = valueParser;
	}

	public List<List<T>> parse(String key, String value) throws KVPParseException {
		// remove embracing brackets
		if (value.startsWith("(") && value.endsWith(")"))
			value = value.substring(1, value.length() - 1);

		String[] lists = value.split(KVPConstants.LIST_DELIMITER);
		List<List<T>> result = new ArrayList<>(lists.length);

		for (String list : lists) {
			String[] items = list.split(KVPConstants.ITEM_DELIMITER);
			List<T> tmp = new ArrayList<>(items.length);
			for (String item : items) {
				if (!item.trim().isEmpty())
					tmp.add(valueParser.parse(key, item));
			}

			if (tmp.isEmpty())
				throw new KVPParseException("The parameter " + key + " must not contain an empty parameter list.", key);

			result.add(tmp);
		}

		return result;
	}
}
