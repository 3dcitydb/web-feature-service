package vcs.citydb.wfs.kvp.parser;

import net.opengis.wfs._2.ResolveValueType;
import vcs.citydb.wfs.exception.KVPParseException;

public class ResolveValueParser extends ValueParser<ResolveValueType> {

	@Override
	public ResolveValueType parse(String key, String value) throws KVPParseException {
		try {
			return ResolveValueType.fromValue(value.trim());
		} catch (IllegalArgumentException e) {
			throw new KVPParseException("The value '" + value + "' is not allowed for the parameter " + key + ".", key);
		}
	}

}
