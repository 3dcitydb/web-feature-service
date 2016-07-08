/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2016
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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ObjectFactory;

import org.citydb.api.concurrent.SingleWorkerPool;
import org.citydb.config.Config;
import org.citydb.modules.citygml.common.database.uid.UIDCache;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.citygml.exporter.util.FeatureProcessor;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryArrayProperty;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.walker.GMLWalker;
import org.citygml4j.util.xml.SAXEventBuffer;

import vcs.citydb.wfs.config.WFSConfig;

public class FeatureMemberWriter implements FeatureProcessor {
	private final SingleWorkerPool<SAXEventBuffer> writerPool;
	private final UIDCacheManager lookupServerManager;
	private final JAXBBuilder jaxbBuilder;
	private final Config exporterConfig;
	private final ObjectFactory wfsFactory;	
	private final JAXBMarshaller jaxbMarshaller;
	
	private final GeometryStripper geometryStripper;
	private boolean writeMemberProperty;

	public FeatureMemberWriter(SingleWorkerPool<SAXEventBuffer> writerPool, 
			UIDCacheManager lookupServerManager, 
			JAXBBuilder jaxbBuilder, 
			WFSConfig wfsConfig,
			Config exporterConfig) {
		this.writerPool = writerPool;
		this.lookupServerManager = lookupServerManager;
		this.jaxbBuilder = jaxbBuilder;
		this.exporterConfig = exporterConfig;
		
		geometryStripper = wfsConfig.getSecurity().isStripGeometry() ? new GeometryStripper() : null;			

		wfsFactory = new ObjectFactory();		
		CityGMLVersion version = Util.toCityGMLVersion(exporterConfig.getProject().getExporter().getCityGMLVersion());
		jaxbMarshaller = jaxbBuilder.createJAXBMarshaller(version);
	}

	public void setWriteMemberProperty(boolean writeMemberProperty) {
		this.writeMemberProperty = writeMemberProperty;
	}

	@Override
	public void process(AbstractFeature abstractFeature) throws FeatureProcessException {

		// security feature: strip geometry from features
		if (geometryStripper != null) {
			abstractFeature.accept(geometryStripper);
			geometryStripper.reset();
		}

		JAXBElement<?> output = null;
		
		if (writeMemberProperty) {
			MemberPropertyType memberProperty = new MemberPropertyType();

			if (!exporterConfig.getInternal().isRegisterGmlIdInCache() || !isFeatureAlreadyExported(abstractFeature)) {
				// TODO: CityGML 1.0 Appearance elements are not global and hence must be wrapped by an AppearanceProperty 
				memberProperty.getContent().add(jaxbMarshaller.marshalJAXBElement(abstractFeature));
			} else
				memberProperty.setHref("#" + abstractFeature.getId());
			
			output = wfsFactory.createMember(memberProperty);
		} else {
			output = jaxbMarshaller.marshalJAXBElement(abstractFeature);
		}
		
		try {
			SAXEventBuffer buffer = new SAXEventBuffer();
			Marshaller marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, writeMemberProperty);				

			if (output != null)
				marshaller.marshal(output, buffer);
			else
				throw new FeatureProcessException("Failed to write feature with gml:id '" + abstractFeature.getId() + "'.");

			if (!buffer.isEmpty())
				writerPool.addWork(buffer);
			else
				throw new FeatureProcessException("Failed to write feature with gml:id '" + abstractFeature.getId() + "'.");
		} catch (JAXBException e) {
			throw new FeatureProcessException("Failed to write feature with gml:id '" + abstractFeature.getId() + "': " + e.getMessage());
		}
	}

	public boolean isFeatureAlreadyExported(AbstractFeature abstractFeature) {
		if (!abstractFeature.isSetId())
			return false;

		UIDCache server = lookupServerManager.getCache(CityGMLClass.ABSTRACT_CITY_OBJECT);
		return server.get(abstractFeature.getId()) != null;
	}
	
	private final class GeometryStripper extends GMLWalker {
		
		@Override
		public <T extends AbstractGeometry> void visit(GeometryArrayProperty<T> arrayProperty) {
			arrayProperty.unsetGeometry();
		}

		@Override
		public <T extends AbstractGeometry> void visit(GeometryProperty<T> geometryProperty) {
			if (geometryProperty.isSetGeometry()) {
				String gmlId = geometryProperty.getGeometry().getId();
				if (gmlId != null)
					geometryProperty.setHref("#" + gmlId);
				
				geometryProperty.unsetGeometry();
			}
		}
	}

}
