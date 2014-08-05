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
