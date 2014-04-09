/*
 * This file is part of the 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright (c) 2014
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 * 
 * The 3D City Database Web Feature Service is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package vcs.citydb.wfs.operation.storedquery;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import net.opengis.wfs._2.Abstract;
import net.opengis.wfs._2.ParameterExpressionType;
import net.opengis.wfs._2.QueryExpressionTextType;
import net.opengis.wfs._2.StoredQueryDescriptionType;
import net.opengis.wfs._2.Title;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.xml.NamespaceFilter;
import de.tub.citydb.util.Util;

public class StoredQueryManager {
	private final StoredQuery DEFAULT_QUERY;
	private final JAXBBuilder jaxbBuilder;
	private final TransformerFactory transformerFactory;
	private final WFSConfig wfsConfig;

	public StoredQueryManager(JAXBBuilder jaxbBuilder, WFSConfig wfsConfig) throws ParserConfigurationException, SAXException {
		this.jaxbBuilder = jaxbBuilder;
		this.wfsConfig = wfsConfig;

		DEFAULT_QUERY = createDefaultStoredQuery();
		transformerFactory = TransformerFactory.newInstance();
	}

	public List<StoredQuery> listStoredQueries(String handle) throws WFSException {
		List<StoredQuery> storedQueries = new ArrayList<StoredQuery>();
		storedQueries.add(DEFAULT_QUERY);

		return storedQueries;
	}

	public StoredQuery getStoredQuery(String id, String handle) throws WFSException {
		if (DEFAULT_QUERY.getId().equals(id))
			return DEFAULT_QUERY;
		
		throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "A stored query with identifier '" + id + "' is not offered by this server.", handle);
	}

	protected JAXBBuilder getJAXBBuilder() {
		return jaxbBuilder;
	}

	protected TransformerFactory getTransformerFactory() {
		return transformerFactory;
	}

	private StoredQuery createDefaultStoredQuery() throws ParserConfigurationException, SAXException {
		// GetFeatureById query according to the WFS 2.0 spec
		StoredQueryDescriptionType description = new StoredQueryDescriptionType();

		description.setId("urn:ogc:def:query:OGC-WFS::GetFeatureById");

		Title queryTitle = new Title();
		queryTitle.setLang("en");
		queryTitle.setValue("Get feature by identifier");
		description.getTitle().add(queryTitle);
		Abstract queryAbstract = new Abstract();
		queryAbstract.setLang("en");
		queryAbstract.setValue("Retrieves a feature by its gml:id.");
		description.getAbstract().add(queryAbstract);

		ParameterExpressionType parameter = new ParameterExpressionType();
		parameter.setName("id");
		parameter.setType(XSDataType.XS_STRING.getName());
		Title parameterTitle = new Title();
		parameterTitle.setLang("en");
		parameterTitle.setValue("Identifier");
		parameter.getTitle().add(parameterTitle);
		Abstract parameterAbstract = new Abstract();
		parameterAbstract.setLang("en");
		parameterAbstract.setValue("The gml:id of the feature to be retrieved.");
		parameter.getAbstract().add(parameterAbstract);
		description.getParameter().add(parameter);

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.newDocument();
		Element query = document.createElementNS(Constants.WFS_NAMESPACE_URI, "Query");

		NamespaceFilter namespaceFilter = new NamespaceFilter();
		boolean multipleVersions = wfsConfig.getFeatureTypes().getVersions().size() > 1;
		List<String> typeNames = new ArrayList<String>();
		for (QName typeName : wfsConfig.getFeatureTypes().getDefaultFeatureTypes()) {
			CityGMLModule module = Modules.getCityGMLModule(typeName.getNamespaceURI());
			String prefix = module.getNamespacePrefix();
			if (multipleVersions)
				prefix += (CityGMLVersion.fromCityGMLModule(module) == CityGMLVersion.v2_0_0) ? "2" : "1";

			typeNames.add(prefix + ':' + typeName.getLocalPart());
			namespaceFilter.startPrefixMapping(prefix, module.getNamespaceURI());
		}

		query.setAttribute("typeNames", Util.collection2string(typeNames, " "));
		Element filter = document.createElementNS(Constants.FES_NAMESPACE_URI, "Filter");
		Element resourceId = document.createElementNS(Constants.FES_NAMESPACE_URI, "fes:ResourceId");
		resourceId.setAttribute("rid", "${id}");
		filter.appendChild(resourceId);
		query.appendChild(filter);

		QueryExpressionTextType queryExpression = new QueryExpressionTextType();
		queryExpression.getContent().add(query);		
		queryExpression.setIsPrivate(false);
		queryExpression.setLanguage("en");
		queryExpression.setReturnFeatureTypes(wfsConfig.getFeatureTypes().getDefaultFeatureTypes());
		queryExpression.setLanguage(StoredQuery.DEFAULT_LANGUAGE);
		description.getQueryExpressionText().add(queryExpression);

		return new StoredQuery(description, namespaceFilter, this);
	}

}
