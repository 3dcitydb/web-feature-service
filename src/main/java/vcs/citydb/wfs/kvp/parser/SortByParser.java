package vcs.citydb.wfs.kvp.parser;

import net.opengis.fes._2.SortByType;
import net.opengis.fes._2.SortOrderType;
import net.opengis.fes._2.SortPropertyType;
import vcs.citydb.wfs.exception.KVPParseException;

import java.util.ArrayList;
import java.util.List;

public class SortByParser extends ValueParser<List<SortByType>> {

	@Override
	public List<SortByType> parse(String key, String value) throws KVPParseException {
		List<SortByType> sortBys = new ArrayList<>();
		ValueListParser<String> listParser = new ValueListParser<>(new StringParser());
		StringParser propertyParser = new StringParser();

		for (List<String> sortByExpressions : listParser.parse(key, value)) {
			SortByType sortBy = new SortByType();

			for (String sortByExpression : sortByExpressions) {
				String[] items = sortByExpression.split("\\s+");
				if (items.length > 2)
					throw new KVPParseException("The value '" + value + "' of the parameter " + key + " must have the form 'PropertyName [ASC|DESC]'.", key);

				propertyParser.parse(key, items[0]);
				SortOrderType sortOrder = null;

				if (items.length == 2) {
					try {
						sortOrder = SortOrderType.valueOf(items[1]);
					} catch (IllegalArgumentException e) {
						throw new KVPParseException("The sorting order '" + items[1] + "' used in the sorting expression '" + value + "' of the parameter " + key + " is not a valid sort order of the form '[ASC|DESC]'.", key);
					}
				}

				SortPropertyType sortProperty = new SortPropertyType();
				sortProperty.setValueReference(items[0]);
				sortProperty.setSortOrder(sortOrder != null ? sortOrder : SortOrderType.ASC);

				sortBy.getSortProperty().add(sortProperty);
			}

			sortBys.add(sortBy);
		}

		return sortBys;
	}
}
