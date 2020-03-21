package vcs.citydb.wfs.kvp;

import net.opengis.wfs._2.DescribeFeatureTypeType;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.KVPParseException;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.kvp.parser.FlatValueListParser;
import vcs.citydb.wfs.kvp.parser.QNameParser;
import vcs.citydb.wfs.kvp.parser.StringParser;

import javax.xml.namespace.QName;
import java.util.Map;

public class DescribeFeatureTypeReader extends KVPRequestReader {
	private final BaseRequestReader baseRequestReader;

	public DescribeFeatureTypeReader(Map<String, String> parameters, WFSConfig wfsConfig) {
		super(parameters, wfsConfig);

		baseRequestReader = new BaseRequestReader();
	}

	@Override
	public DescribeFeatureTypeType readRequest() throws WFSException {
		DescribeFeatureTypeType wfsRequest = new DescribeFeatureTypeType();
		baseRequestReader.read(wfsRequest, parameters);

		try {
			if (parameters.containsKey(KVPConstants.TYPE_NAME))
				wfsRequest.getTypeName().addAll(new FlatValueListParser<QName>(new QNameParser(getNamespaces())).parse(KVPConstants.TYPE_NAME, parameters.get(KVPConstants.TYPE_NAME)));

			if (parameters.containsKey(KVPConstants.OUTPUT_FORMAT))
				wfsRequest.setOutputFormat(new StringParser().parse(KVPConstants.OUTPUT_FORMAT, parameters.get(KVPConstants.OUTPUT_FORMAT)));

		} catch (KVPParseException e) {
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), e.getCause());
		}

		return wfsRequest;
	}

}