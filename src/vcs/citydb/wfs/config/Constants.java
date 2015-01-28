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
package vcs.citydb.wfs.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Constants {
	public static final String CONFIG_PATH = "/WEB-INF";
	public static final String CONFIG_FILE = "config.xml";
	public static final String CONFIG_SCHEMA_PATH = "WebContent/WEB-INF/schemas/config";
	public static final String CONFIG_SCHEMA_FILE = CONFIG_SCHEMA_PATH + "/config.xsd";
	public static final String XML_SCHEMAS_PATH = "/WEB-INF/schemas";
	public static final String CITYGML_1_0_SCHEMAS_PATH = XML_SCHEMAS_PATH + "/citygml/1.0";
	public static final String CITYGML_2_0_SCHEMAS_PATH = XML_SCHEMAS_PATH + "/citygml/2.0";
	public static final String MIME_TYPES_PATH = "/WEB-INF/mimetypes";	
	public static final String MIME_TYPES_FILES = "mime.types";
	public static final String LOG_PATH = "/WEB-INF";
	public static final String LOG_FILE = "wfs.log";
	
	public static final String INIT_ERROR_ATTRNAME = "init_error";
	
	public static final String WFS_SERVICE_PATH = "/wfs";
	public static final String WFS_SERVICE_STRING = "WFS";
	public static final List<String> SUPPORTED_WFS_VERSIONS = new ArrayList<String>(Arrays.asList(new String[]{"2.0.2", "2.0.0"}));
	public static final String DEFAULT_WFS_VERSION = "2.0.2";	
	
	public static final String WFS_NAMESPACE_URI = "http://www.opengis.net/wfs/2.0";
	public static final String WFS_NAMESPACE_PREFIX = "wfs";
	public static final String WFS_SCHEMA_LOCATION = "http://schemas.opengis.net/wfs/2.0/wfs.xsd";
	public static final String FES_NAMESPACE_URI = "http://www.opengis.net/fes/2.0";
	public static final String FES_NAMESPACE_PREFIX = "fes";
	public static final String FES_SCHEMA_LOCATION = "http://schemas.opengis.net/filter/2.0/filterAll.xsd";
	public static final String OWS_NAMESPACE_URI = "http://www.opengis.net/ows/1.1";
	public static final String OWS_NAMESPACE_PREFIX = "ows";
	public static final String OWS_SCHEMA_LOCATION = "http://schemas.opengis.net/ows/1.1.0/owsAll.xsd";
	public static final String GML_3_2_1_NAMESPACE_URI = "http://www.opengis.net/gml/3.2";
	public static final String GML_3_3_NAMESPACE_URI = "http://www.opengis.net/gml/3.3";
}