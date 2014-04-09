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
package vcs.citydb.wfs.operation.describefeaturetype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import net.opengis.wfs._2.DescribeFeatureTypeType;

import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionMessage;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.operation.FeatureTypeHandler;
import vcs.citydb.wfs.util.LoggerUtil;
import de.tub.citydb.log.Logger;

public class DescribeFeatureTypeHandler {
	private final Logger log = Logger.getInstance();
	private final WFSConfig wfsConfig;

	private final BaseRequestHandler baseRequestHandler;
	private final FeatureTypeHandler featureTypeHandler;

	public DescribeFeatureTypeHandler(WFSConfig wfsConfig) {
		this.wfsConfig = wfsConfig;
		
		baseRequestHandler = new BaseRequestHandler();
		featureTypeHandler = new FeatureTypeHandler();
	}

	public void doOperation(DescribeFeatureTypeType wfsRequest,
			ServletContext servletContext,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		log.info(LoggerUtil.getLogMessage(request, "Accepting DescribeFeatureType request."));
		final String operationHandle = wfsRequest.getHandle();

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// check output format
		if (wfsRequest.isSetOutputFormat() && !wfsConfig.getOperations().getDescribeFeatureType().supportsOutputFormat(wfsRequest.getOutputFormat())) {
			WFSExceptionMessage message = new WFSExceptionMessage(WFSExceptionCode.OPTION_NOT_SUPPORTED);
			message.addExceptionText("The output format of a DescribeFeatureType request must match one of the following formats:");
			message.addExceptionTexts(wfsConfig.getOperations().getDescribeFeatureType().getOutputFormatAsString());
			message.setLocator(operationHandle);
			
			throw new WFSException(message);
		}
		
		Set<QName> featureTypeNames = featureTypeHandler.getFeatureTypeNames(wfsRequest.getTypeName(), true, operationHandle);

		String schemaFileLocation = getSchemaFileLocation(featureTypeNames, operationHandle);
		if (schemaFileLocation == null)
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to map feature type on XML Schema file.", operationHandle);

		try {
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");

			BufferedReader reader = new BufferedReader(new InputStreamReader(servletContext.getResourceAsStream(schemaFileLocation)));
			PrintWriter writer = response.getWriter();
			String line;

			while ((line = reader.readLine()) != null)
				writer.println(line);

		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to read local XML Schema file.", operationHandle, e);
		}

		log.info(LoggerUtil.getLogMessage(request, "DescribeFeatureType operation successfully finished."));
	}

	private String getSchemaFileLocation(Set<QName> featureTypeNames, String handle) {
		CityGMLVersion version = wfsConfig.getFeatureTypes().getDefaultVersion();
		if (!featureTypeNames.isEmpty())
			version = CityGMLVersion.fromCityGMLModule(Modules.getCityGMLModule(featureTypeNames.iterator().next().getNamespaceURI()));
		
		StringBuilder schemaLocation = version == CityGMLVersion.v2_0_0 ? new StringBuilder(Constants.CITYGML_2_0_SCHEMAS_PATH) : 
			new StringBuilder(Constants.CITYGML_1_0_SCHEMAS_PATH);

		schemaLocation.append('/');
		
		if (featureTypeNames.isEmpty())
			return schemaLocation.append("profiles/base/CityGML.xsd").toString();

		Set<CityGMLModuleType> moduleTypes = new HashSet<CityGMLModuleType>();
		for (QName featureTypeName : featureTypeNames) {
			Module module = Modules.getModule(featureTypeName.getNamespaceURI());
			if (!(module.getType() instanceof CityGMLModuleType))
				throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "The feature type '" + featureTypeName.toString() + "' is not part of CityGML.", handle);

			moduleTypes.add((CityGMLModuleType)module.getType());
		}

		if (moduleTypes.size() > 1)
			return schemaLocation.append("profiles/base/CityGML.xsd").toString();

		CityGMLModuleType moduleType = moduleTypes.iterator().next();
		switch (moduleType) {
		case APPEARANCE:
			return schemaLocation.append("appearance.xsd").toString();
		case BUILDING:
			return schemaLocation.append("building.xsd").toString();
		case BRIDGE:
			return schemaLocation.append("bridge.xsd").toString();
		case TUNNEL:
			return schemaLocation.append("tunnel.xsd").toString();
		case CITY_FURNITURE:
			return schemaLocation.append("cityFurniture.xsd").toString();
		case CORE:
			return schemaLocation.append("cityGMLBase.xsd").toString();
		case CITY_OBJECT_GROUP:
			return schemaLocation.append("cityObjectGroup.xsd").toString();
		case GENERICS:
			return schemaLocation.append("generics.xsd").toString();
		case LAND_USE:
			return schemaLocation.append("landUse.xsd").toString();
		case RELIEF:
			return schemaLocation.append("relief.xsd").toString();
		case TEXTURED_SURFACE:
			return schemaLocation.append("texturedSurface.xsd").toString();
		case TRANSPORTATION:
			return schemaLocation.append("transportation.xsd").toString();
		case VEGETATION:
			return schemaLocation.append("vegetation.xsd").toString();
		case WATER_BODY:
			return schemaLocation.append("waterBody.xsd").toString();
		default:
			return null;
		}		
	}
}
