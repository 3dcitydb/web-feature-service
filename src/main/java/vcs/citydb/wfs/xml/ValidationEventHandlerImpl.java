package vcs.citydb.wfs.xml;

import org.citydb.config.project.global.LogLevel;
import org.citydb.log.Logger;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

public class ValidationEventHandlerImpl implements ValidationEventHandler {
	private final Logger log = Logger.getInstance();
	private boolean isValid = true;
	private String cause = null;

	@Override
	public boolean handleEvent(ValidationEvent event) {
		StringBuilder msg = new StringBuilder();
		LogLevel type = LogLevel.ERROR;

		switch (event.getSeverity()) {
		case ValidationEvent.FATAL_ERROR:
		case ValidationEvent.ERROR:
			msg.append("Invalid XML content");
			type = LogLevel.ERROR;
			isValid = false;
			break;
		case ValidationEvent.WARNING:
			msg.append("Warning");
			type = LogLevel.WARN;
			break;
		}

		if (event.getLocator() != null) {
			msg.append(" at [").append(event.getLocator().getLineNumber())
			.append(", ").append(event.getLocator().getColumnNumber()).append("]");
		}

		msg.append(": ").append(event.getMessage());		
		log.log(type, msg.toString());
		
		if (!isValid)
			cause = msg.toString();
		
		return isValid;
	}

	public boolean isValid() {
		return isValid;
	}
	
	public String getCause() {
		return cause;
	}
}
