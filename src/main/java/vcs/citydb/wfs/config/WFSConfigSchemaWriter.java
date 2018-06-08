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
