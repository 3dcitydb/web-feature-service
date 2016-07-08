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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vcs.citydb.wfs.config.Constants;

public class WFSException extends Exception {
	private static final long serialVersionUID = -1620130047924173953L;
	
	private List<WFSExceptionMessage> exceptionMessages = new ArrayList<WFSExceptionMessage>();
	private final String LANGUAGE = "en";

	public WFSException(String exceptionText) {
		super(exceptionText);
		exceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.NO_APPLICABLE_CODE, exceptionText));
	}
	
	public WFSException(String message, Throwable cause) {
		this(WFSExceptionCode.NO_APPLICABLE_CODE, message, cause);
	}
	
	public WFSException(WFSExceptionCode exceptionCode, String exceptionText, String locator, Throwable cause) {
		super(exceptionText, cause);
		WFSExceptionMessage exceptionMessage = new WFSExceptionMessage(exceptionCode);
		exceptionMessage.addExceptionText(exceptionText);
		
		String causeMessage = null;
		while (cause != null && (causeMessage = cause.getMessage()) == null)
			cause = cause.getCause();
		
		exceptionMessage.addExceptionText(causeMessage);
		exceptionMessage.setLocator(locator);
		exceptionMessages.add(exceptionMessage);
	}
	
	public WFSException(WFSExceptionCode exceptionCode, String message, Throwable cause) {
		this(exceptionCode, message, null, cause);
	}
	
	public WFSException(Throwable cause) {
		super(cause);
		
		String causeMessage = null;
		while (cause != null && (causeMessage = cause.getMessage()) == null)
			cause = cause.getCause();
		
		exceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.NO_APPLICABLE_CODE, causeMessage));
	}
	
	public WFSException(WFSExceptionCode exceptionCode, String exceptionText, String locator) {
		exceptionMessages.add(new WFSExceptionMessage(exceptionCode, exceptionText, locator));
	}
	
	public WFSException(WFSExceptionCode exceptionCode, String exceptionText) {
		this(exceptionCode, exceptionText, (String)null);
	}
	
	public WFSException(WFSExceptionMessage exceptionMessage) {
		exceptionMessages.add(exceptionMessage);
	}
	
	public WFSException(WFSExceptionMessage... exceptionMessages) {
		this.exceptionMessages = Arrays.asList(exceptionMessages);
	}
	
	public WFSException(List<WFSExceptionMessage> exceptionMessages) {
		this.exceptionMessages = exceptionMessages;
	}
	
	public WFSException(Throwable cause, WFSExceptionMessage exceptionMessage) {
		super(cause);
		exceptionMessages.add(exceptionMessage);
	}
	
	public WFSException(Throwable cause, WFSExceptionMessage... exceptionMessages) {
		super(cause);
		this.exceptionMessages = Arrays.asList(exceptionMessages);
	}
	
	public WFSException(Throwable cause, List<WFSExceptionMessage> exceptionMessages) {
		super(cause);
		this.exceptionMessages = exceptionMessages;
	}
	
	public List<WFSExceptionMessage> getExceptionMessages() {
		return exceptionMessages;
	}
	
	public void addExceptionMessage(WFSExceptionMessage message) {
		exceptionMessages.add(message);
	}
	
	public void addExceptionMessages(List<WFSExceptionMessage> messages) {
		exceptionMessages.addAll(messages);
	}
	
	public String getVersion() {
		return Constants.DEFAULT_WFS_VERSION;
	}
	
	public String getLanguage() {
		return LANGUAGE;
	}
	
}
