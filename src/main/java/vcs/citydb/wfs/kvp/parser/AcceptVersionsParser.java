package vcs.citydb.wfs.kvp.parser;

import java.util.List;

import net.opengis.ows._1.AcceptVersionsType;
import vcs.citydb.wfs.exception.KVPParseException;

public class AcceptVersionsParser extends ValueParser<AcceptVersionsType> {

	@Override
	public AcceptVersionsType parse(String key, String value) throws KVPParseException {
		AcceptVersionsType acceptVersions = new AcceptVersionsType();
		
		List<String> versions = new FlatValueListParser<String>(new StringParser()).parse(key, value);
		acceptVersions.setVersion(versions);
		
		return acceptVersions;
	}
	
}
