/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
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
