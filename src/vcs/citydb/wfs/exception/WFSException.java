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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vcs.citydb.wfs.config.Constants;


public class WFSException extends RuntimeException {
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
	
	public String getVersion() {
		return Constants.WFS_VERSION_STRING;
	}
	
	public String getLanguage() {
		return LANGUAGE;
	}
	
}
