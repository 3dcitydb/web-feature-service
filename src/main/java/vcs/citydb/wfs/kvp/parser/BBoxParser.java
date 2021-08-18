package vcs.citydb.wfs.kvp.parser;

import net.opengis.fes._2.BBOXType;
import net.opengis.fes._2.FilterType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import vcs.citydb.wfs.exception.KVPParseException;

import java.math.BigInteger;
import java.util.List;

public class BBoxParser extends ValueParser<FilterType> {
	private final net.opengis.fes._2.ObjectFactory fesFactory;
	private final net.opengis.gml.ObjectFactory gmlFactory;
	
	public BBoxParser(net.opengis.fes._2.ObjectFactory fesFactory, net.opengis.gml.ObjectFactory gmlFactory) {
		this.fesFactory = fesFactory;
		this.gmlFactory = gmlFactory;
	}
	
	@Override
	public FilterType parse(String key, String value) throws KVPParseException {
		ValueListParser<String> listParser = new ValueListParser<>(new StringParser());
		
		List<List<String>> tmp = listParser.parse(key, value);
		if (tmp.size() > 1)
			throw new KVPParseException("The parameter " + key + " may only encode a single bounding box value.", key);
						
		EnvelopeType envelope = new EnvelopeType();
		try {
			List<String> items = tmp.get(0);
			int dim = items.size() / 2;			

			envelope.setSrsDimension(BigInteger.valueOf(dim));			
			if ((items.size() & 1) != 0)
				envelope.setSrsName(items.get(items.size() - 1));
			
			DirectPositionType lowerCorner = new DirectPositionType();
			DirectPositionType upperCorner = new DirectPositionType();
			
			for (int i = 0; i < dim; i++) {
				lowerCorner.getValue().add(Double.valueOf(items.get(i)));
				upperCorner.getValue().add(Double.valueOf(items.get(i + dim)));
			}
			
			envelope.setLowerCorner(lowerCorner);
			envelope.setUpperCorner(upperCorner);
			
		} catch (NumberFormatException e) {
			throw new KVPParseException("The value '" + value + "' is not allowed for the parameter " + key + ".", key);
		}
		
		BBOXType bbox = new BBOXType();
		bbox.getExpressionOrAny().add(gmlFactory.createEnvelope(envelope));
				
		FilterType bboxFilter = new FilterType();
		bboxFilter.setSpatialOps(fesFactory.createBBOX(bbox));
		
		return bboxFilter;
	}

}
