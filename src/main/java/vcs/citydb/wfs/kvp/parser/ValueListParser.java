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

	@Override
	public List<List<T>> parse(String key, String value) throws KVPParseException {		
		String[] lists = value.split(KVPConstants.LIST_DELIMITER);
		List<List<T>> result = new ArrayList<List<T>>(lists.length);

		for (int i = 0; i < lists.length; i++) {
			String[] items = lists[i].split(KVPConstants.ITEM_DELIMITER);
			List<T> tmp = new ArrayList<T>(items.length);
			for (int j = 0; j < items.length; j++) {
				String item = items[j];

				if (i == 0 && j == 0)
					item = item.replaceFirst("^\\(", "");
				if (i == lists.length - 1 && j == items.length - 1)
					item = item.replaceFirst("\\)$", "");

				if (!item.trim().isEmpty())
					tmp.add(valueParser.parse(key, item));
			}
			
			if (tmp.isEmpty())
				throw new KVPParseException("The parameter " + key + " must not contain an empty parameter list.");

			result.add(tmp);
		}

		return result;
	}

}
