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
