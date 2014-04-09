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
