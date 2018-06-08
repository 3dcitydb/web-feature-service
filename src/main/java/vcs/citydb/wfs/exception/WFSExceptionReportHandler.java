package vcs.citydb.wfs.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import net.opengis.ows._1.ExceptionReport;
import net.opengis.ows._1.ExceptionType;
import net.opengis.ows._1.ObjectFactory;
import vcs.citydb.wfs.config.Constants;

public class WFSExceptionReportHandler {
	private final Logger log = Logger.getInstance();
	private final CityGMLBuilder cityGMLBuilder;
	private final ObjectFactory owsFactory;

	public WFSExceptionReportHandler(CityGMLBuilder cityGMLBuilder) {
		this.cityGMLBuilder = cityGMLBuilder;
		owsFactory = new ObjectFactory();		
	}

	public ExceptionReport getExceptionReport(WFSException wfsException, HttpServletRequest request, boolean writeToLog) {
		ExceptionReport exceptionReport = owsFactory.createExceptionReport();

		exceptionReport.setLang(wfsException.getLanguage());
		exceptionReport.setVersion(wfsException.getVersion());

		for (WFSExceptionMessage message : wfsException.getExceptionMessages()) {		
			ExceptionType exceptionType = owsFactory.createExceptionType();

			exceptionType.setExceptionCode(message.getExceptionCode().getValue());
			exceptionType.setLocator(message.getLocator());

			for (String text : message.getExceptionTexts()) {
				exceptionType.getExceptionText().add(text);

				if (writeToLog) {
					StringBuilder logMessage = new StringBuilder();
					logMessage.append('[').append(message.getExceptionCode().getValue())
					.append(", ").append(request.getRemoteAddr());
					if (message.getLocator() != null)
						logMessage.append(", ").append(message.getLocator());

					logMessage.append("]: ").append(text);				
					log.error(logMessage.toString());
				}
			}

			exceptionReport.getException().add(exceptionType);
		}

		return exceptionReport;
	}

	public synchronized void sendErrorResponse(WFSException wfsException, HttpServletRequest request, HttpServletResponse response) throws IOException {
		ExceptionReport exceptionReport = getExceptionReport(wfsException, request, true);

		response.setContentType("text/xml");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(wfsException.getExceptionMessages().get(0).getExceptionCode().getHttpStatusCode());	

		// prepare SAXWriter
		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setWriteEncoding(true);
		saxWriter.setIndentString("  ");

		saxWriter.setPrefix(Constants.OWS_NAMESPACE_PREFIX, Constants.OWS_NAMESPACE_URI);
		saxWriter.setSchemaLocation(Constants.OWS_NAMESPACE_URI, Constants.OWS_SCHEMA_LOCATION);

		// marshall JAXB object
		try {
			saxWriter.setOutput(response.getOutputStream(), StandardCharsets.UTF_8.name());
			Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
			marshaller.marshal(exceptionReport, saxWriter);
		} catch (JAXBException | IOException e) {
			if (!response.isCommitted())
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			// flush SAX writer
			try {
				saxWriter.flush();
			} catch (SAXException e) {
				//
			}
		}
	}

}
