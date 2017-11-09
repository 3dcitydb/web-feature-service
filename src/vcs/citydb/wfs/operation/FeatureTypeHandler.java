/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vcs.citydb.wfs.operation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.citydb.api.registry.ObjectRegistry;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.AppearanceModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.GenericsModule;
import org.citygml4j.xml.schema.ElementDecl;
import org.citygml4j.xml.schema.Schema;
import org.citygml4j.xml.schema.SchemaHandler;
import org.citygml4j.xml.schema.SchemaWalker;

import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.feature.FeatureType;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSElementDecl;

public class FeatureTypeHandler {
	private final SchemaHandler schemaHandler;
	private final WFSConfig wfsConfig;

	public FeatureTypeHandler() {
		schemaHandler = (SchemaHandler)ObjectRegistry.getInstance().lookup(SchemaHandler.class.getName());
		wfsConfig = (WFSConfig)ObjectRegistry.getInstance().lookup(WFSConfig.class.getName());
	}

	public Set<QName> getFeatureTypeNames(Collection<QName> featureTypeNames, boolean canBeEmpty, String handle) throws WFSException {
		Set<QName> result = new HashSet<QName>();

		for (QName featureTypeName : featureTypeNames) {
			if (featureTypeName == null)
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Failed to interpret feature type name as XML qualified name.", handle);

			validateFeatureTypeName(featureTypeName, handle);

			// check whether the requested feature type is advertised
			if (!wfsConfig.getFeatureTypes().getAdvertisedFeatureTypes().contains(featureTypeName))
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The requested feature type '" + featureTypeName.toString() + "' is not advertised.", handle);

			result.add(featureTypeName);			
		}

		// check whether the query must contain at least one feature type name
		if (!canBeEmpty && result.isEmpty())
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The operation requires at least one feature type name to be provided.", handle);

		// check whether all feature types share the same CityGML version
		CityGMLVersion version = null;
		for (QName name : result) {
			CityGMLVersion featureVersion = CityGMLVersion.fromCityGMLModule(Modules.getCityGMLModule(name.getNamespaceURI()));
			
			if (version == null) {
				version = featureVersion;
				continue;
			} else if (featureVersion != version)
				throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Mixing feature types from different CityGML versions is not supported.", handle);
		}

		return result;
	}

	public Set<QName> getFeatureTypeNames(Collection<String> featureTypeNames, NamespaceContext namespaceContext, boolean canBeEmpty, String handle) throws WFSException {
		Set<QName> result = new HashSet<QName>();

		for (String featureTypeName : featureTypeNames) {
			String candidate = featureTypeName;
			boolean isSchemaElementFunction = false;

			// TODO: although parsing of the schema-element() function is already
			// implemented, the function itself is not supported in the following.
			if (featureTypeName.startsWith("schema-element")) {
				Pattern pattern = Pattern.compile("schema-element\\((.+)\\)$");
				Matcher matcher = pattern.matcher(featureTypeName);
				if (matcher.matches()) {
					candidate = matcher.group(1);
					isSchemaElementFunction = true;
				} else
					throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid schema-element() function: '" + featureTypeName + "'.", handle);
			}

			String namespacePrefix = null;
			String namespaceURI = null;
			String localPart = null;

			String[] parts = candidate.split(":");
			if (parts.length == 1) {
				namespacePrefix = XMLConstants.DEFAULT_NS_PREFIX;
				localPart = parts[0];
			} else if (parts.length == 2) {
				namespacePrefix = parts[0];
				localPart = parts[1];
			} else
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid feature type name: '" + candidate + "'.", handle);

			namespaceURI = namespaceContext.getNamespaceURI(namespacePrefix);
			if (namespaceURI == null)
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The feature type name '" + candidate + "' lacks a namespace declaration.", handle);

			QName qName = new QName(namespaceURI, localPart);

			if (!isSchemaElementFunction)
				result.add(qName);
			else
				result.addAll(resolveSchemaElementFunction(qName, handle));
		}

		return getFeatureTypeNames(result, canBeEmpty, handle);
	}

	public boolean isPropertyElementOf(QName featureTypeName, final QName propertyName, String handle) throws WFSException {
		ElementDecl element = getElementDecl(featureTypeName, handle);

		SchemaWalker schemaWalker = new SchemaWalker() {
			@Override
			public void elementDecl(XSElementDecl child) {
				if (child.getName().equals(propertyName.getLocalPart()) && 
						child.getTargetNamespace().equals(propertyName.getNamespaceURI())) {
					setShouldWalk(false);
				}
			}

			@Override
			public void attributeUse(XSAttributeUse use) {
				// avoid visiting attribute use
			}

		};

		element.getXSElementDecl().getType().visit(schemaWalker);
		boolean isPropertyElement = !schemaWalker.shouldWalk();

		// TOOO: check for generic attributes and appearance attributes
		if (!isPropertyElement) {
			String namespaceURI = propertyName.getNamespaceURI();
			isPropertyElement = namespaceURI.equals(GenericsModule.v1_0_0.getNamespaceURI()) ||
					namespaceURI.equals(AppearanceModule.v1_0_0.getNamespaceURI());
		}

		return isPropertyElement;
	}

	public CityGMLClass getCityGMLClass(QName featureTypeName) {
		Module module = Modules.getModule(featureTypeName.getNamespaceURI());
		if (module instanceof CityGMLModule) {
			CityGMLModule cityGMLModule = (CityGMLModule)module;
			return CityGMLClass.fromModelClass(cityGMLModule.getFeatureElementClass(featureTypeName.getLocalPart()));
		}

		return CityGMLClass.UNDEFINED;
	}

	private ElementDecl getElementDecl(QName featureTypeName, String handle) throws WFSException {
		Schema schema = schemaHandler.getSchema(featureTypeName.getNamespaceURI());
		if (schema == null)
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The feature type '" + featureTypeName.toString() + "' is not part of CityGML.", handle);

		// TODO: in CityGML 1.0, app:Appearance is not declared as global element and hence
		// would not fulfill the following test. However, appearances are not supported so far anyways...
		ElementDecl element = schema.getGlobalElementDecl(featureTypeName.getLocalPart());
		if (element == null)
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The feature type '" + featureTypeName.getLocalPart() + "' is not defined in the associated namespace '" + featureTypeName.getNamespaceURI() + "'.", handle);

		return element;
	}

	private boolean validateFeatureTypeName(QName featureTypeName, String handle) throws WFSException {
		return getElementDecl(featureTypeName, handle) != null;
	}

	private Set<QName> resolveSchemaElementFunction(QName featureTypeName, String handle) throws WFSException {
		CityGMLVersion version = CityGMLVersion.fromCityGMLModule(Modules.getCityGMLModule(featureTypeName.getNamespaceURI()));					
		if (!wfsConfig.getFeatureTypes().getVersions().contains(version))
			throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Feature types from CityGML version '" + version + "' are not advertised.", handle);
		if (featureTypeName.getLocalPart().equals("_CityObject")) {
			Set<QName> types = new HashSet<QName>();
			for (FeatureType type : wfsConfig.getFeatureTypes().getFeatureTypes())
				types.add(type.getQName(version));
			return types;
		}
		throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Only the abstract super type '_CityObject' may be used as parameter for the schema-element() function.", handle);
	}
}
