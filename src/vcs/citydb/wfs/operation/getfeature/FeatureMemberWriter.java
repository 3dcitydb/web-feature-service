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
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXEventBuffer;

public class FeatureMemberWriter implements FeatureProcessor {
	private final SingleWorkerPool<SAXEventBuffer> writerPool;
	private final UIDCacheManager lookupServerManager;
	private final JAXBBuilder jaxbBuilder;
	private final Config exporterConfig;
	private final ObjectFactory wfsFactory;	
	private final JAXBMarshaller jaxbMarshaller;

	public FeatureMemberWriter(SingleWorkerPool<SAXEventBuffer> writerPool, 
			UIDCacheManager lookupServerManager, 
			JAXBBuilder jaxbBuilder, 
			Config exporterConfig) {
		this.writerPool = writerPool;
		this.lookupServerManager = lookupServerManager;
		this.jaxbBuilder = jaxbBuilder;
		this.exporterConfig = exporterConfig;

		wfsFactory = new ObjectFactory();		
		CityGMLVersion version = Util.toCityGMLVersion(exporterConfig.getProject().getExporter().getCityGMLVersion());
		jaxbMarshaller = jaxbBuilder.createJAXBMarshaller(version);
	}

	@Override
	public void process(AbstractFeature abstractFeature) throws FeatureProcessException {
		MemberPropertyType memberProperty = new MemberPropertyType();

		if (!exporterConfig.getInternal().isRegisterGmlIdInCache() || !isFeatureAlreadyExported(abstractFeature)) {
			// TODO: CityGML 1.0 Appearance elements are not global and hence must be wrapped by an AppearanceProperty 
			memberProperty.getContent().add(jaxbMarshaller.marshalJAXBElement(abstractFeature));
		} else
			memberProperty.setHref("#" + abstractFeature.getId());

		try {
			SAXEventBuffer buffer = new SAXEventBuffer();
			Marshaller marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);				

			JAXBElement<?> memberPropertyElement = wfsFactory.createMember(memberProperty);
			if (memberPropertyElement != null)
				marshaller.marshal(memberPropertyElement, buffer);
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

}
