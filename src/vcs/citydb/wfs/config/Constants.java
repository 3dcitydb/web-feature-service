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