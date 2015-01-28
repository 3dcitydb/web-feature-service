package vcs.citydb.wfs.operation.getfeature;

import java.util.WeakHashMap;

import org.citydb.api.concurrent.SingleWorkerPool;
import org.citydb.config.Config;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.exporter.util.FeatureProcessor;
import org.citydb.modules.citygml.exporter.util.FeatureProcessorFactory;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXEventBuffer;

import vcs.citydb.wfs.config.WFSConfig;

public class FeatureMemberWriterFactory implements FeatureProcessorFactory {
	private final SingleWorkerPool<SAXEventBuffer> writerPool;
	private final UIDCacheManager lookupServerManager;
	private final JAXBBuilder jaxbBuilder;
	private final WFSConfig wfsConfig;
	private final Config exporterConfig;
	
	private WeakHashMap<FeatureMemberWriter, Void> writers;
	private boolean writeMemberProperty;
			
	public FeatureMemberWriterFactory(SingleWorkerPool<SAXEventBuffer> writerPool, 
			UIDCacheManager lookupServerManager, 
			JAXBBuilder jaxbBuilder, 
			WFSConfig wfsConfig,
			Config exporterConfig) {
		this.writerPool = writerPool;
		this.lookupServerManager = lookupServerManager;
		this.jaxbBuilder = jaxbBuilder;
		this.wfsConfig = wfsConfig;
		this.exporterConfig = exporterConfig;
		
		writers = new WeakHashMap<FeatureMemberWriter, Void>();
	}
	
	@Override
	public FeatureProcessor createFeatureProcessor() {
		FeatureMemberWriter writer = new FeatureMemberWriter(writerPool, lookupServerManager, jaxbBuilder, wfsConfig, exporterConfig);
		writer.setWriteMemberProperty(writeMemberProperty);
		writers.put(writer, null);
		return writer;
	}
	
	public void setWriteMemberProperty(boolean writeMemberProperty) {
		this.writeMemberProperty = writeMemberProperty;
		for (FeatureMemberWriter writer : writers.keySet())
			writer.setWriteMemberProperty(writeMemberProperty);
	}

}
