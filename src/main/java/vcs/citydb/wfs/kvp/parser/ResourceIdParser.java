package vcs.citydb.wfs.kvp.parser;

import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.ObjectFactory;
import net.opengis.fes._2.ResourceIdType;
import vcs.citydb.wfs.exception.KVPParseException;

import java.util.ArrayList;
import java.util.List;

public class ResourceIdParser extends ValueParser<List<FilterType>> {
	private final ObjectFactory fesFactory;
	
	public ResourceIdParser(ObjectFactory fesFactory) {
		this.fesFactory = fesFactory;
	}
	
	@Override
	public List<FilterType> parse(String key, String value) throws KVPParseException {
		List<FilterType> filters = new ArrayList<>();
		ValueListParser<String> listParser = new ValueListParser<>(new StringParser());
		
		for (List<String> ids : listParser.parse(key, value)) {
			FilterType filter = new FilterType();
			
			for (String id : ids) {
				ResourceIdType resourceId = new ResourceIdType();
				resourceId.setRid(id);
				filter.get_Id().add(fesFactory.create_Id(resourceId));
			}
			
			filters.add(filter);
		}
		
		return filters;
	}

}
