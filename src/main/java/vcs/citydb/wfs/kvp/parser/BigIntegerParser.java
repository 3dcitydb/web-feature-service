package vcs.citydb.wfs.kvp.parser;

import java.math.BigInteger;

import vcs.citydb.wfs.exception.KVPParseException;

public class BigIntegerParser extends ValueParser<BigInteger> {

	@Override
	public BigInteger parse(String key, String value) throws KVPParseException {
		try {
			return new BigInteger(value.trim());
		} catch (NumberFormatException e) {
			throw new KVPParseException("The value '" + value + "' is not allowed for the parameter " + key + ".");
		}
	}

}
