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
package vcs.citydb.wfs.xml;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.citydb.api.log.LogLevel;
import org.citydb.log.Logger;

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
