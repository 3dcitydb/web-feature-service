package vcs.citydb.wfs.operation.getfeature.citygml;

import net.opengis.wfs._2.FeatureCollectionType;
import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.TruncatedResponse;
import org.citydb.config.Config;
import org.citydb.core.operation.common.cache.IdCache;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.cache.IdCacheType;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.core.writer.SequentialWriter;
import org.citydb.core.writer.XMLWriterWorkerFactory;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.internal.xml.TransformerChain;
import org.citygml4j.util.internal.xml.TransformerChainFactory;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.citygml4j.util.xml.SAXWriter;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.operation.getfeature.FeatureWriter;
import vcs.citydb.wfs.util.GeometryStripper;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import java.math.BigInteger;
import java.time.LocalDateTime;

public class CityGMLWriter implements FeatureWriter, EventHandler {
	private final SAXWriter saxWriter;
	private final CityGMLVersion version;
	private final TransformerChainFactory transformerChainFactory;
	private final GeometryStripper geometryStripper;
	private final IdCacheManager idCacheManager;
	private final Object eventChannel;
	private final InternalConfig internalConfig;

	private final SingleWorkerPool<SAXEventBuffer> writerPool;
	private final CityGMLBuilder cityGMLBuilder;
	private final JAXBMarshaller jaxbMarshaller;
	private final ObjectFactory wfsFactory;
	private final DatatypeFactory datatypeFactory;
	private final AdditionalObjectsHandler additionalObjectsHandler;
	private final EventDispatcher eventDispatcher;

	private int level;
	private boolean isWriteSingleFeature;
	private boolean checkForDuplicates;
	private boolean useSequentialWriting;
	private SequentialWriter<SAXEventBuffer> sequentialWriter;

	public CityGMLWriter(
			SAXWriter saxWriter,
			CityGMLVersion version,
			TransformerChainFactory transformerChainFactory,
			GeometryStripper geometryStripper,
			IdCacheManager idCacheManager,
			Object eventChannel,
			InternalConfig internalConfig,
			Config config) throws DatatypeConfigurationException {
		this.saxWriter = saxWriter;
		this.version = version;
		this.transformerChainFactory = transformerChainFactory;
		this.geometryStripper = geometryStripper;
		this.idCacheManager = idCacheManager;
		this.eventChannel = eventChannel;
		this.internalConfig = internalConfig;

		cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();
		jaxbMarshaller = cityGMLBuilder.createJAXBMarshaller(version);
		wfsFactory = new ObjectFactory();
		datatypeFactory = DatatypeFactory.newInstance();
		additionalObjectsHandler = new AdditionalObjectsHandler(saxWriter, version, cityGMLBuilder, transformerChainFactory, eventChannel);

		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
		
		int queueSize = config.getExportConfig().getResources().getThreadPool().getMaxThreads() * 2;
		writerPool = new SingleWorkerPool<>(
				"citygml_writer_pool",
				new XMLWriterWorkerFactory(saxWriter, eventDispatcher),
				queueSize,
				false);

		writerPool.setEventSource(eventChannel);
		writerPool.prestartCoreWorkers();
	}

	@Override
	public void setWriteSingleFeature(boolean isWriteSingleFeature) {
		this.isWriteSingleFeature = isWriteSingleFeature;
	}

	@Override
	public boolean supportsFlatHierarchies() {
		return !isWriteSingleFeature;
	}

	@Override
	public void useIndentation(boolean useIndentation) {
		saxWriter.setIndentString(useIndentation ? " " : "");
	}

	@Override
	public void startFeatureCollection(long matchNo, long returnNo, String previous, String next) throws FeatureWriteException {
		level++;
		writeFeatureCollection(matchNo, returnNo, previous, next, WriteMode.HEAD);
	}

	@Override
	public void startFeatureCollection(long matchNo, long returnNo) throws FeatureWriteException {
		startFeatureCollection(matchNo, returnNo, null, null);
	}

	@Override
	public void endFeatureCollection() throws FeatureWriteException {
		writeFeatureCollection(0, 0, null, null, WriteMode.TAIL);
		level--;
		
		// we only have to check for duplicates after the first set of features
		checkForDuplicates = internalConfig.isRegisterGmlIdInCache();
	}

	@Override
	public void startAdditionalObjects() throws FeatureWriteException {
		try {
			writerPool.join();
			additionalObjectsHandler.startAdditionalObjects();
		} catch (SAXException | InterruptedException e) {
			throw new FeatureWriteException("Failed to marshal additional objects collection element.", e);
		}
	}

	@Override
	public void endAdditionalObjects() throws FeatureWriteException {
		try {
			additionalObjectsHandler.endAdditionalObjects();
		} catch (SAXException e) {
			throw new FeatureWriteException("Failed to marshal additional objects collection element.", e);
		}
	}

	@Override
	public long setSequentialWriting(boolean useSequentialWriting)  {
		long sequenceId = 0;

		if (useSequentialWriting) {
			if (this.useSequentialWriting)
				sequenceId = sequentialWriter.getCurrentSequenceId();
			else if (sequentialWriter != null)
				sequenceId = sequentialWriter.reset();
			else
				sequentialWriter = new SequentialWriter<>(writerPool);
		}

		this.useSequentialWriting = useSequentialWriting;
		return sequenceId;
	}

	@Override
	public void write(AbstractFeature feature, long sequenceId) throws FeatureWriteException {
		// security feature: strip geometry from features
		if (geometryStripper != null)
			feature.accept(geometryStripper);

		if (feature.hasLocalProperty(CoreConstants.EXPORT_AS_ADDITIONAL_OBJECT)) {
			// according to the WFS 2.0 spec, additional objects shall be placed
			// within the wfs:additionalObjects collection element. So we need to
			// cache them here.
			additionalObjectsHandler.cacheObject(feature);
			return;
		}

		JAXBElement<?> output = null;
		if (!isWriteSingleFeature) {
			MemberPropertyType memberProperty = new MemberPropertyType();

			if (!checkForDuplicates || !isFeatureAlreadyExported(feature)) {
				if (!(feature instanceof Appearance) || version == CityGMLVersion.v2_0_0) {
					JAXBElement<?> element = jaxbMarshaller.marshalJAXBElement(feature);
					if (element != null)
						memberProperty.getContent().add(element);
				} else {
					// appearance elements are not global XML elements in CityGML 1.0. Thus, we have
					// to wrap them with a property element and use an intermediate DOM element.
					Element element = jaxbMarshaller.marshalDOMElement(new AppearanceMember((Appearance) feature));
					if (element != null && element.hasChildNodes())
						memberProperty.getContent().add(element.getFirstChild());
				}

				if (memberProperty.isSetContent())
					output = wfsFactory.createMember(memberProperty);
			} else {
				memberProperty.setHref("#" + feature.getId());
				output = wfsFactory.createMember(memberProperty);
			}
		} else
			output = jaxbMarshaller.marshalJAXBElement(feature);

		SAXEventBuffer buffer = new SAXEventBuffer();
		try {
			if (output != null) {
				Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, !isWriteSingleFeature);

				if (transformerChainFactory == null)
					marshaller.marshal(output, buffer);
				else {
					TransformerChain chain = transformerChainFactory.buildChain();
					chain.tail().setResult(new SAXResult(buffer));
					chain.head().startDocument();
					marshaller.marshal(output, chain.head());
					chain.head().endDocument();
				}
			} else
				throw new FeatureWriteException("Failed to write feature with gml:id '" + feature.getId() + "'.");
		} catch (JAXBException | SAXException | TransformerConfigurationException e) {
			throw new FeatureWriteException("Failed to write feature with gml:id '" + feature.getId() + "'.", e);
		}

		if (buffer.isEmpty())
			throw new FeatureWriteException("Failed to write feature with gml:id '" + feature.getId() + "'.");

		if (!useSequentialWriting)
			writerPool.addWork(buffer);
		else {
			try {
				sequentialWriter.write(buffer, sequenceId);
			} catch (InterruptedException e) {
				throw new FeatureWriteException("Failed to write feature with gml:id '" + feature.getId() + "'.", e);
			}
		}
	}

	@Override
	public void updateSequenceId(long sequenceId) throws FeatureWriteException {
		if (useSequentialWriting) {
			try {
				sequentialWriter.updateSequenceId(sequenceId);
			} catch (InterruptedException e) {
				throw new FeatureWriteException("Failed to update sequence id.", e);
			}
		}
	}

	@Override
	public void writeAdditionalObjects() throws FeatureWriteException {
		try {
			writerPool.join();
			if (additionalObjectsHandler.hasAdditionalObjects())
				additionalObjectsHandler.writeObjects();
		} catch (InterruptedException e) {
			throw new FeatureWriteException("Failed to marshal additional objects.", e);
		}
	}

	@Override
	public void writeTruncatedResponse(TruncatedResponse truncatedResponse) throws FeatureWriteException {
		try {
			SAXEventBuffer buffer = new SAXEventBuffer();
			Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
			marshaller.marshal(truncatedResponse, buffer); 
			writerPool.addWork(buffer);
		} catch (JAXBException e) {
			throw new FeatureWriteException("Failed to marshal truncated response element.", e);
		}
	}

	@Override
	public void close() throws FeatureWriteException {
		try {
			writerPool.shutdownAndWait();
		} catch (InterruptedException e) {
			throw new FeatureWriteException("Failed to close CityGML response writer.", e);
		} finally {
			if (!writerPool.isTerminated())
				writerPool.shutdownNow();

			if (additionalObjectsHandler.hasAdditionalObjects())
				additionalObjectsHandler.cleanCache();

			eventDispatcher.removeEventHandler(this);
		}
	}
	
	private boolean isFeatureAlreadyExported(AbstractFeature feature) {
		if (!feature.isSetId())
			return false;

		IdCache cache = idCacheManager.getCache(IdCacheType.OBJECT);
		return cache.get(feature.getId()) != null;
	}

	private void writeFeatureCollection(long matchNo, long returnNo, String previous, String next, WriteMode writeMode) throws FeatureWriteException {
		try {
			FeatureCollectionType featureCollection = new FeatureCollectionType();

			if (writeMode == WriteMode.HEAD) {
				featureCollection.setTimeStamp(getTimeStamp());
				featureCollection.setNumberMatched(matchNo != Constants.UNKNOWN_NUMBER_MATCHED ? String.valueOf(matchNo) : "unknown");
				featureCollection.setNumberReturned(BigInteger.valueOf(returnNo));

				if (level == 1) {
					featureCollection.setPrevious(previous);
					featureCollection.setNext(next);
				}
			}

			JAXBElement<?> output;
			if (level == 1) {
				output = wfsFactory.createFeatureCollection(featureCollection);
			} else {
				MemberPropertyType member = new MemberPropertyType();
				member.getContent().add(wfsFactory.createFeatureCollection(featureCollection));
				output = wfsFactory.createMember(member);
			}

			SAXEventBuffer buffer = new SAXEventBuffer();
			SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(new QName(Constants.WFS_NAMESPACE_URI, "FeatureCollection"), buffer, writeMode);
			Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
			marshaller.marshal(output, fragmentWriter);

			writerPool.addWork(buffer);
		} catch (JAXBException e) {
			throw new FeatureWriteException("Failed to marshal feature collection element.", e);
		}
	}

	private XMLGregorianCalendar getTimeStamp() {
		LocalDateTime date = LocalDateTime.now();
		return datatypeFactory.newXMLGregorianCalendar(
				date.getYear(),
				date.getMonthValue(),
				date.getDayOfMonth(),
				date.getHour(),
				date.getMinute(),
				date.getSecond(),
				DatatypeConstants.FIELD_UNDEFINED,
				DatatypeConstants.FIELD_UNDEFINED);
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel && sequentialWriter != null)
			sequentialWriter.interrupt();
	}

}
