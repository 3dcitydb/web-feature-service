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

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import de.tub.citydb.log.Logger;

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
