package vcs.citydb.wfs.config;

import org.citydb.config.util.ConfigConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class WFSConfigSchemaWriter {

	public static void main(String[] args) throws Exception {
		System.out.print("Generating XML schema in " + Constants.CONFIG_SCHEMA_FILE + "... ");
		
		JAXBContext ctx = JAXBContext.newInstance(WFSConfig.class);
		ctx.generateSchema(new SchemaOutputResolver() {
			
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				File file;

				if (namespaceUri.equals(ConfigConstants.CITYDB_CONFIG_NAMESPACE_URI))
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
