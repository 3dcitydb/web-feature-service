package vcs.citydb.wfs.operation.filter;

import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.Namespace;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureTypeHandler {
	private final SchemaMapping schemaMapping;
	private final WFSConfig wfsConfig;

	private CityGMLVersion version;

	public FeatureTypeHandler() {
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		wfsConfig = ObjectRegistry.getInstance().lookup(WFSConfig.class);
	}

	public FeatureType getFeatureType(QName featureTypeName, boolean onlyAdvertised, String parameterName, String handle) throws WFSException {
		if (featureTypeName == null)
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Failed to interpret feature type name as XML qualified name.", parameterName);

		FeatureType featureType = schemaMapping.getFeatureType(featureTypeName);
		if (featureType == null)
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "'" + featureTypeName + "' is not a valid CityGML feature type.", parameterName);

		// check whether the requested feature type is advertised
		if (onlyAdvertised && !wfsConfig.getFeatureTypes().contains(featureTypeName))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The requested feature type '" + featureTypeName + "' is not advertised.", parameterName);

		// get CityGML version
		version = featureType.getSchema().getCityGMLVersion(featureTypeName.getNamespaceURI());
		if (version == null)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to retrieve CityGML version for feature type '" + featureTypeName + "'.", handle);

		return featureType;
	}

	public FeatureType getFeatureType(QName featureTypeName, String parameterName, String handle) throws WFSException {
		return getFeatureType(featureTypeName, true, parameterName, handle);
	}

	public Set<FeatureType> getFeatureTypes(Collection<QName> featureTypeNames, boolean onlyAdvertised, boolean canBeEmpty, String parameterName, String handle) throws WFSException {
		Set<FeatureType> result = new HashSet<>();
		CityGMLVersion featureVersion = null;

		for (QName featureTypeName : featureTypeNames) {
			FeatureType featureType = getFeatureType(featureTypeName, onlyAdvertised, parameterName, handle);

			// check whether all feature types share the same CityGML version
			if (featureVersion == null)
				featureVersion = version;
			else if (version != featureVersion)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Mixing feature types from different CityGML versions is not supported.", handle);

			result.add(featureType);
		}

		// check whether the query must contain at least one feature type name
		if (!canBeEmpty && result.isEmpty())
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The operation requires at least one feature type name to be provided.", parameterName);

		return result;
	}

	public Set<FeatureType> getFeatureTypes(Collection<QName> featureTypeNames, boolean canBeEmpty, String parameterName, String handle) throws WFSException {
		return getFeatureTypes(featureTypeNames, true, canBeEmpty, parameterName, handle);
	}

	public Set<FeatureType> getFeatureTypes(Collection<String> featureTypeNames, NamespaceContext namespaceContext, boolean onlyAdvertised, boolean canBeEmpty, String parameterName, String handle) throws WFSException {
		Set<FeatureType> result = new HashSet<>();
		CityGMLVersion featureVersion = null;

		for (String featureTypeName : featureTypeNames) {
			String candidate = featureTypeName;
			boolean isSchemaElementFunction = false;

			// check for schema-element() function
			if (featureTypeName.startsWith("schema-element")) {
				Pattern pattern = Pattern.compile("schema-element\\((.+)\\)$");
				Matcher matcher = pattern.matcher(featureTypeName);
				if (matcher.matches()) {
					candidate = matcher.group(1);
					isSchemaElementFunction = true;
				} else
					throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid use of schema-element() function: '" + featureTypeName + "'.", "schema-element()");
			}

			String namespacePrefix;
			String namespaceURI;
			String localPart;

			String[] parts = candidate.split(":");
			if (parts.length == 1) {
				namespacePrefix = XMLConstants.DEFAULT_NS_PREFIX;
				localPart = parts[0];
			} else if (parts.length == 2) {
				namespacePrefix = parts[0];
				localPart = parts[1];
			} else
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid feature type name: '" + candidate + "'.", parameterName);

			namespaceURI = namespaceContext.getNamespaceURI(namespacePrefix);
			if (namespaceURI == null)
				throw new WFSException(WFSExceptionCode.OPERATION_PARSING_FAILED, "The prefix '" + namespacePrefix + "' lacks a namespace declaration.", handle);

			QName qName = new QName(namespaceURI, localPart);

			if (!isSchemaElementFunction)
				result.add(getFeatureType(qName, onlyAdvertised, parameterName, handle));
			else
				result.addAll(resolveSchemaElementFunction(qName, onlyAdvertised, parameterName,handle));

			// check whether all feature types share the same CityGML version
			if (featureVersion == null)
				featureVersion = version;
			else if (version != featureVersion)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Mixing feature types from different CityGML versions is not supported.", handle);
		}

		// check whether the result set must contain at least one feature type
		if (!canBeEmpty && result.isEmpty())
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The operation requires at least one feature type name to be provided.", parameterName);

		return result;
	}

	public Set<FeatureType> getFeatureTypes(Collection<String> featureTypeNames, NamespaceContext namespaceContext, boolean canBeEmpty, String parameterName, String handle) throws WFSException {
		return getFeatureTypes(featureTypeNames, namespaceContext, true, canBeEmpty, parameterName, handle);
	}

	private Set<FeatureType> resolveSchemaElementFunction(QName featureTypeName, boolean onlyAdvertised, String parameterName, String handle) throws WFSException {
		FeatureType featureType = schemaMapping.getFeatureType(featureTypeName);
		if (featureType == null)
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "'" + featureTypeName + "' is not a valid CityGML feature type.", parameterName);

		Set<FeatureType> result = new HashSet<>();
		version = featureType.getSchema().getCityGMLVersion(featureTypeName.getNamespaceURI());
		if (version == null)
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to retrieve CityGML version for feature type '" + featureTypeName + "'.", handle);

		// create list of possible substitutes for the requested feature type
		List<FeatureType> candidates = featureType.listSubTypes(true);
		if (!featureType.isAbstract())
			candidates.add(featureType);

		// reduce substitutes to advertised feature types only
		for (FeatureType candidate : candidates) {
			String localPart = candidate.getPath();
			Namespace namespace = candidate.getSchema().getNamespace(version);
			if (namespace == null)
				continue;

			QName name = new QName(namespace.getURI(), localPart);
			if (!onlyAdvertised || wfsConfig.getFeatureTypes().contains(name))
				result.add(candidate);
		}

		if (result.isEmpty())
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "The schema-element function for '" + featureTypeName + "' evaluates to an empty set of advertised feature types.", handle);

		return result;
	}
	
	public FeatureType getFeatureType(AbstractFeature feature) {
		return schemaMapping.getFeatureType(Util.getObjectClassId(feature.getClass()));
	}

	public ObjectType getObjectType(AbstractGML object) {
		return schemaMapping.getObjectType(Util.getObjectClassId(object.getClass()));
	}

	public AbstractObjectType<?> getAbstractObjectType(AbstractGML object) {
		return object instanceof AbstractFeature ? getFeatureType((AbstractFeature)object) : getObjectType(object);
	}
	
	public String getXMLName(AbstractGML object) {
		StringBuilder xmlName = new StringBuilder();
		AbstractObjectType<?> type = getAbstractObjectType(object);			
		if (type != null)
			xmlName.append(type.getSchema().getXMLPrefix()).append(":").append(type.getPath());
		else
			xmlName.append((object instanceof CityGML) ? ((CityGML)object).getCityGMLClass().toString() : object.getGMLClass().toString());
		
		return xmlName.toString();
	}

	public CityGMLVersion getCityGMLVersion() {
		return version;
	}

}
