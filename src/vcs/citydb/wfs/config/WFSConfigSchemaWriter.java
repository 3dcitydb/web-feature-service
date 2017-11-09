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

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class WFSConfigSchemaWriter {

	public static void main(String[] args) throws Exception {
		System.out.print("Generting XML schema in " + Constants.CONFIG_SCHEMA_FILE + "... ");
		
		JAXBContext ctx = JAXBContext.newInstance(WFSConfig.class);
		ctx.generateSchema(new SchemaOutputResolver() {
			
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				File file;

				if (namespaceUri.equals("http://www.3dcitydb.org/importer-exporter/config"))
					file = new File(Constants.CONFIG_SCHEMA_FILE);
				else
					file = new File(Constants.CONFIG_SCHEMA_PATH + "/ows/" + suggestedFileName);
				
				file.getAbsoluteFile().getParentFile().mkdirs();
				
				StreamResult res = new StreamResult();
				res.setSystemId(file.toURI().toString());
				return res;
			}
			
		});
		
		System.out.println("finished.");
	}

}
