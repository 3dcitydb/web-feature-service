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
package vcs.citydb.wfs.exception;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.ows._1.ExceptionReport;
import net.opengis.ows._1.ExceptionType;
import net.opengis.ows._1.ObjectFactory;

import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;

public class WFSExceptionReportHandler {
	private final Logger log = Logger.getInstance();
	private final JAXBBuilder jaxbBuilder;
	private final ObjectFactory owsFactory;

	public WFSExceptionReportHandler(JAXBBuilder jaxbBuilder) {
		this.jaxbBuilder = jaxbBuilder;
		owsFactory = new ObjectFactory();		
	}

	public synchronized void sendErrorResponse(WFSException wfsException, HttpServletRequest request, HttpServletResponse response) throws IOException {
		ExceptionReport exceptionReport = owsFactory.createExceptionReport();

		exceptionReport.setLang(wfsException.getLanguage());
		exceptionReport.setVersion(wfsException.getVersion());

		for (WFSExceptionMessage message : wfsException.getExceptionMessages()) {		
			ExceptionType exceptionType = owsFactory.createExceptionType();

			exceptionType.setExceptionCode(message.getExceptionCode().getValue());
			exceptionType.setLocator(message.getLocator());

			for (String text : message.getExceptionTexts()) {
				exceptionType.getExceptionText().add(text);

				StringBuilder logMessage = new StringBuilder();
				logMessage.append('[').append(message.getExceptionCode().getValue())
				.append(", ").append(request.getRemoteAddr());				
				if (message.getLocator() != null)
					logMessage.append(", ").append(message.getLocator());

				logMessage.append("]: ").append(text);				
				log.error(logMessage.toString());
			}

			exceptionReport.getException().add(exceptionType);
		}

		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(wfsException.getExceptionMessages().get(0).getExceptionCode().getHttpStatusCode());	

		// prepare SAXWriter
		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setWriteEncoding(true);
		saxWriter.setIndentString("  ");

		saxWriter.setPrefix(Constants.OWS_NAMESPACE_PREFIX, Constants.OWS_NAMESPACE_URI);
		saxWriter.setSchemaLocation(Constants.OWS_NAMESPACE_URI, Constants.OWS_SCHEMA_LOCATION);

		// marshall JAXB object
		try {
			saxWriter.setOutput(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
			Marshaller marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
			marshaller.marshal(exceptionReport, saxWriter);
		} catch (JAXBException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			// close SAX writer. this also closes the servlet output stream.
			try {
				saxWriter.close();
			} catch (SAXException e) {
				//
			}
		}
	}

}
