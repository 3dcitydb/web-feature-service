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
