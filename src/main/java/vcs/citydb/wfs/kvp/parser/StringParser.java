package vcs.citydb.wfs.kvp.parser;

import vcs.citydb.wfs.exception.KVPParseException;

public class StringParser extends ValueParser<String> {

	@Override
	public String parse(String key, String value) throws KVPParseException {
		return value.trim();
	}

}
