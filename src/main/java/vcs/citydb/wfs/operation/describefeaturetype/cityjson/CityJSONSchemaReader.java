package vcs.citydb.wfs.operation.describefeaturetype.cityjson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.SchemaReaderException;
import vcs.citydb.wfs.operation.describefeaturetype.SchemaReader;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class CityJSONSchemaReader implements SchemaReader {
	private Set<FeatureType> featureTypes;
	private ServletContext servletContext;
	
	@Override
	public String getMimeType() {
		return "application/json";
	}

	@Override
	public void initializeContext(Set<FeatureType> featureTypes, CityGMLVersion version, ServletContext servletContext) throws SchemaReaderException {
		this.featureTypes = featureTypes;
		this.servletContext = servletContext;
		
		List<String> unsupported = Arrays.asList("TransportationComplex", "Track");
		for (FeatureType featureType : featureTypes) {
			if (unsupported.contains(featureType.getPath()))
				throw new SchemaReaderException("The feature type '" + featureType.getPath() + "' is not supported by CityJSON.");
		}
	}

	@Override
	public InputStream openSchema() throws SchemaReaderException, IOException {
		if (!featureTypes.isEmpty()) {
			try {
				Map<String, String> mappings = new HashMap<>();
				mappings.put("TransportSquare", "Square");
				
				Map<String, List<String>> hierarchies = new HashMap<>();
				hierarchies.put("Building", Arrays.asList("BuildingPart", "BuildingInstallation"));
				hierarchies.put("Bridge", Arrays.asList("BridgePart", "BridgeInstallation", "BridgeConstructionElement"));
				hierarchies.put("Tunnel", Arrays.asList("TunnelPart", "TunnelInstallation"));

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonReader reader = new JsonReader(new InputStreamReader(servletContext.getResourceAsStream(Constants.CITYJSON_SCHEMA_PATH + "/cityjson-v06.schema.json")));
				JsonObject schema = gson.fromJson(reader, JsonObject.class);

				JsonObject definitions = schema.get("definitions").getAsJsonObject();
				JsonArray references = schema.get("properties").getAsJsonObject()
						.get("CityObjects").getAsJsonObject()
						.get("additionalProperties").getAsJsonObject()
						.get("oneOf").getAsJsonArray();			

				Map<String, JsonElement> cityObjects = new HashMap<>();
				for (JsonElement reference1 : references) {
					JsonObject reference = reference1.getAsJsonObject();
					String value = reference.get("$ref").getAsString();
					String typeName = value.substring(value.lastIndexOf("/") + 1, value.length());
					cityObjects.put(typeName, reference);
				}
				
				for (Entry<String, JsonElement> entry : cityObjects.entrySet()) {
					String typeName = entry.getKey();
					if (mappings.containsKey(typeName))
						typeName = mappings.get(typeName);
					
					boolean found = false;
					for (FeatureType featureType : featureTypes) {
						if (featureType.getPath().equals(typeName)) {
							found = true;
							break;
						}
					}
					
					if (!found) {
						if (hierarchies.values().stream().flatMap(List::stream).collect(Collectors.toList()).contains(typeName))
							continue;
						
						definitions.remove(entry.getKey());
						references.remove(entry.getValue());
						
						if (hierarchies.containsKey(entry.getKey())) {
							for (String nested : hierarchies.get(entry.getKey())) {
								definitions.remove(nested);
								references.remove(cityObjects.get(nested));
							}
						}
					}
				}

				return new ByteArrayInputStream(gson.toJson(schema).getBytes(StandardCharsets.UTF_8));
			} catch (Throwable e) {
				//
			}
		}

		return servletContext.getResourceAsStream(Constants.CITYJSON_SCHEMA_PATH + "/cityjson-v06.schema.json");
	}



}
