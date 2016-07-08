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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.wfs._2.DescribeStoredQueriesResponseType;
import net.opengis.wfs._2.DescribeStoredQueriesType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.QueryExpressionTextType;
import net.opengis.wfs._2.StoredQueryDescriptionType;

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

public class DescribeStoredQueriesHandler {
	private final Logger log = Logger.getInstance();

	private final BaseRequestHandler baseRequestHandler;
	private final StoredQueryManager storedQueryManager;
	private final Marshaller marshaller;
	private final ObjectFactory wfsFactory;

	public DescribeStoredQueriesHandler(JAXBBuilder jaxbBuilder) throws JAXBException {
		baseRequestHandler = new BaseRequestHandler();
		storedQueryManager = (StoredQueryManager)ObjectRegistry.getInstance().lookup(StoredQueryManager.class.getName());
		wfsFactory = new ObjectFactory();

		marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
	}

	public void doOperation(DescribeStoredQueriesType wfsRequest,
			HttpServletRequest request,
			HttpServletResponse response) throws WFSException {

		log.info(LoggerUtil.getLogMessage(request, "Accepting DescribeStoredQueries request."));
		final String operationHandle = wfsRequest.getHandle();

		// check base service parameters
		baseRequestHandler.validate(wfsRequest);

		// get stored queries to be described
		List<StoredQuery> storedQueries = null;
		if (wfsRequest.isSetStoredQueryId()) {
			storedQueries = new ArrayList<StoredQuery>();
			for (String id : wfsRequest.getStoredQueryId())
				storedQueries.add(storedQueryManager.getStoredQuery(id, operationHandle));
		} else
			storedQueries = storedQueryManager.listStoredQueries(operationHandle);

		final SAXWriter saxWriter = new SAXWriter();

		try {
			// generate response
			DescribeStoredQueriesResponseType describeStoredQueriesResponse = new DescribeStoredQueriesResponseType();
			
			for (StoredQuery storedQuery : storedQueries) {				
				StoredQueryDescriptionType description = storedQuery.getStoredQueryDescription();

				// suppress private query expression texts
				for (QueryExpressionTextType expressionText : description.getQueryExpressionText()) {
					if (expressionText.isIsPrivate())
						expressionText.setContent(Collections.emptyList());
				}

				describeStoredQueriesResponse.getStoredQueryDescription().add(description);
			}

			JAXBElement<DescribeStoredQueriesResponseType> responseElement = wfsFactory.createDescribeStoredQueriesResponse(describeStoredQueriesResponse);

			// write response
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");

			saxWriter.setWriteEncoding(true);
			saxWriter.setIndentString("  ");
			
			saxWriter.setPrefix(Constants.WFS_NAMESPACE_PREFIX, Constants.WFS_NAMESPACE_URI);
			saxWriter.setPrefix(Constants.FES_NAMESPACE_PREFIX, Constants.FES_NAMESPACE_URI);
			saxWriter.setPrefix("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);		
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

			log.info(LoggerUtil.getLogMessage(request, "DescribeStoredQueries operation successfully finished."));
		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal JAXB error occurred whilst marshalling the response document.", e);
		} catch (IOException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal SAX error occurred whilst marshalling the response document.", e);
		} catch (SAXException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close the SAX writer..", e);			
		}
	}

}
