package vcs.citydb.wfs.exception;

import vcs.citydb.wfs.config.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		
		while (cause != null) {
			String causeMessage = cause.getMessage();
			if (causeMessage != null)
				exceptionMessage.addExceptionText(causeMessage);
			
			cause = cause.getCause();
		}
		
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
