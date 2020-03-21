package vcs.citydb.wfs.kvp.parser;

import vcs.citydb.wfs.exception.KVPParseException;

import java.util.ArrayList;
import java.util.List;

public class FlatValueListParser<T> extends ValueParser<List<T>> {
private final ValueParser<T> valueParser;
	
	public FlatValueListParser(ValueParser<T> valueParser) {
		this.valueParser = valueParser;
	}
	
	@Override
	public List<T> parse(String key, String value) throws KVPParseException {
		List<T> result = new ArrayList<T>();
		ValueListParser<String> listParser = new ValueListParser<String>(new StringParser());
		
		List<List<String>> lists = listParser.parse(key, value);
		if (lists.size() > 1)
			throw new KVPParseException("Multiple lists are not supported for the parameter " + key);
		
		for (String item : lists.get(0))
			result.add(valueParser.parse(key, item));
		
		return result;
	}

}
