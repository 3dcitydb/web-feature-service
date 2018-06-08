package vcs.citydb.wfs.kvp.parser;

import vcs.citydb.wfs.exception.KVPParseException;

public abstract class ValueParser<T> {
	public abstract T parse(String key, String value) throws KVPParseException;
}
