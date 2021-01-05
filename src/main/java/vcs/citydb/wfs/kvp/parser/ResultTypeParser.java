package vcs.citydb.wfs.kvp.parser;

import net.opengis.wfs._2.ResultTypeType;
import vcs.citydb.wfs.exception.KVPParseException;

public class ResultTypeParser extends ValueParser<ResultTypeType> {

	@Override
	public ResultTypeType parse(String key, String value) throws KVPParseException {
		try {
			return ResultTypeType.fromValue(value.trim());
		} catch (IllegalArgumentException e) {
			throw new KVPParseException("The value '" + value + "' is not allowed for the parameter " + key + ".", key);
		}
	}

}
