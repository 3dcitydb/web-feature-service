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
import java.util.Collection;
import java.util.List;

public class WFSExceptionMessage {
	private final WFSExceptionCode exceptionCode;
	private List<String> exceptionTexts;
	private String locator;
	
	public WFSExceptionMessage(WFSExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
	
	public WFSExceptionMessage(WFSExceptionCode exceptionCode, String exceptionText) {
		this(exceptionCode);
		addExceptionText(exceptionText);
	}
	
	public WFSExceptionMessage(WFSExceptionCode exceptionCode, String exceptionText, String locator) {
		this(exceptionCode, exceptionText);
		this.locator = locator;
	}
	
	public WFSExceptionMessage(WFSExceptionCode exceptionCode, List<String> exceptionTexts) {
		this(exceptionCode);
		this.exceptionTexts = exceptionTexts;
	}
	
	public WFSExceptionMessage(WFSExceptionCode exceptionCode, List<String> exceptionTexts, String locator) {
		this(exceptionCode, exceptionTexts);
		this.locator = locator;
	}

	public WFSExceptionCode getExceptionCode() {
		return exceptionCode;
	}

	public List<String> getExceptionTexts() {
		return exceptionTexts;
	}

	public String getLocator() {
		return locator;
	}
	
	public void addExceptionText(String exceptionText) {
		if (exceptionTexts == null)
			exceptionTexts = new ArrayList<String>();
		
		exceptionTexts.add(exceptionText);
	}
	
	public void addExceptionTexts(Collection<String> exceptionTexts) {
		if (this.exceptionTexts == null)
			this.exceptionTexts = new ArrayList<String>();
		
		this.exceptionTexts.addAll(exceptionTexts);
	}
	
	public void setLocator(String locator) {
		this.locator = locator;
	}
	
}
