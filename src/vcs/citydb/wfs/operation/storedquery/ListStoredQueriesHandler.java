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
package vcs.citydb.wfs.operation.storedquery;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.wfs._2.ListStoredQueriesResponseType;
import net.opengis.wfs._2.ListStoredQueriesType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.StoredQueryListItemType;

import org.citydb.api.registry.ObjectRegistry;
import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.operation.BaseRequestHandler;
import vcs.citydb.wfs.util.LoggerUtil;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

public class ListStoredQueriesHandler {
	private final Logger log = Logger.getInstance();

	private final BaseRequestHandler baseRequestHandler;
	private final StoredQueryManager storedQueryManager;
	private final Marshaller marshaller;
	private final ObjectFactory wfsFactory;

	public ListStoredQueriesHandler(JAXBBuilder jaxbBuilder) throws JAXBException {
		baseRequestHandler = new BaseRequestHandler();
		storedQueryManager = (StoredQueryManager)ObjectRegistry.getInstance().lookup(StoredQueryManager.class.getName());
		wfsFactory = new ObjectFactory();

		marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
	}

	public void doOperation(ListStoredQueriesType wfsRequest,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		log.info(LoggerUtil.getLogMessage(request, "Accepting ListStoredQueries request."));
		final String operationHandle = wfsRequest.getHandle();

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// get stored queries offered by server
		List<StoredQuery> storedQueries = storedQueryManager.listStoredQueries(operationHandle);

		final SAXWriter saxWriter = new SAXWriter();

		try {
			// generate stored queries list
			ListStoredQueriesResponseType listStoredQueriesResponse = new ListStoredQueriesResponseType();
			for (StoredQuery storedQuery : storedQueries) {
				StoredQueryListItemType listItem = new StoredQueryListItemType();
				listItem.setId(storedQuery.getId());
				listItem.setTitle(storedQuery.getTitle());
				listItem.setReturnFeatureType(storedQuery.getReturnFeatureTypeNames());

				listStoredQueriesResponse.getStoredQuery().add(listItem);
			}

			JAXBElement<ListStoredQueriesResponseType> responseElement = wfsFactory.createListStoredQueriesResponse(listStoredQueriesResponse);

			// write response
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");

			saxWriter.setWriteEncoding(true);
			saxWriter.setIndentString("  ");
			saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			saxWriter.setSchemaLocation(Constants.WFS_NAMESPACE_URI, Constants.WFS_SCHEMA_LOCATION);

			saxWriter.setOutput(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
			marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
				public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
					return saxWriter.getPrefix(namespaceUri);
				}
			});
			
			marshaller.marshal(responseElement, saxWriter);

			// close SAX writer. this also closes the servlet output stream.
			saxWriter.close();

			log.info(LoggerUtil.getLogMessage(request, "ListStoredQueriesHandler operation successfully finished."));
		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal JAXB error occurred whilst marshalling the response document.", e);
		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal SAX error occurred whilst marshalling the response document.", e);
		} catch (SAXException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close the SAX writer..", e);			
		}
	}

}
