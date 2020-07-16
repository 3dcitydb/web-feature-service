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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

				Gson gson = new GsonBuilder().create();
				JsonReader reader = new JsonReader(new InputStreamReader(servletContext.getResourceAsStream(Constants.CITYJSON_SCHEMA_PATH + "/cityjson-v1.0.1.min.schema.json")));
				JsonObject schema = gson.fromJson(reader, JsonObject.class);

				Map<String, JsonElement> cityObjects = new LinkedHashMap<>();
				JsonArray definitions = schema.get("properties").getAsJsonObject()
						.get("CityObjects").getAsJsonObject()
						.get("additionalProperties").getAsJsonObject()
						.get("oneOf").getAsJsonArray();

				for (Iterator<JsonElement> iter = definitions.iterator(); iter.hasNext(); ) {
					JsonElement cityObject = iter.next();
					if (cityObject.isJsonObject() && cityObject.getAsJsonObject().has("allOf")) {
						String typeName = cityObject.getAsJsonObject()
								.get("allOf").getAsJsonArray().get(1).getAsJsonObject()
								.get("properties").getAsJsonObject()
								.get("type").getAsJsonObject()
								.get("enum").getAsJsonArray().get(0).getAsString();

						cityObjects.put(mappings.getOrDefault(typeName, typeName), cityObject);
						iter.remove();
					}
				}

				for (FeatureType featureType : featureTypes) {
					String typeName = featureType.getPath();
					JsonElement cityObject = cityObjects.get(typeName);

					if (cityObject != null) {
						definitions.add(cityObject);

						// add children
						for (String childName : hierarchies.getOrDefault(typeName, Collections.emptyList())) {
							JsonElement child = cityObjects.get(childName);
							if (child != null)
								definitions.add(child);
						}
					}
				}

				return new ByteArrayInputStream(gson.toJson(schema).getBytes(StandardCharsets.UTF_8));
			} catch (Throwable e) {
				//
			}
		}

		return servletContext.getResourceAsStream(Constants.CITYJSON_SCHEMA_PATH + "/cityjson-v1.0.1.min.schema.json");
	}
}
