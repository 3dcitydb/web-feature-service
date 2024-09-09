package vcs.citydb.wfs.exception;

import vcs.citydb.wfs.config.Constants;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class WFSException extends Exception {
    private static final long serialVersionUID = -1620130047924173953L;

    private Deque<WFSExceptionMessage> exceptionMessages = new ArrayDeque<>();
    private final String LANGUAGE = "en";

    WFSException(WFSException other) {
        super(other.getMessage(), other.getCause());
        exceptionMessages = new ArrayDeque<>(other.exceptionMessages);
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

    public WFSException(WFSExceptionCode exceptionCode, String exceptionText, String locator) {
        super(exceptionText);
        exceptionMessages.add(new WFSExceptionMessage(exceptionCode, exceptionText, locator));
    }

    public WFSException(WFSExceptionCode exceptionCode, String exceptionText) {
        this(exceptionCode, exceptionText, (String) null);
    }

    public WFSException(WFSExceptionMessage exceptionMessage) {
        super(!exceptionMessage.getExceptionTexts().isEmpty() ? exceptionMessage.getExceptionTexts().get(0) : "");
        exceptionMessages.add(exceptionMessage);
    }

    public Collection<WFSExceptionMessage> getExceptionMessages() {
        return exceptionMessages;
    }

    public void addExceptionMessage(WFSExceptionMessage exceptionMessage) {
        if (exceptionMessage != null)
            exceptionMessages.add(exceptionMessage);
    }

    public void addExceptionMessages(Collection<WFSExceptionMessage> exceptionMessages) {
        for (WFSExceptionMessage exceptionMessage : exceptionMessages) {
            if (exceptionMessage != null)
                this.exceptionMessages.add(exceptionMessage);
        }
    }

    public WFSExceptionMessage getFirstExceptionMessage() {
        return exceptionMessages.getFirst();
    }

    public void addFirstExceptionMessage(WFSExceptionMessage exceptionMessage) {
        if (exceptionMessage != null)
            exceptionMessages.addFirst(exceptionMessage);
    }

    public String getVersion() {
        return Constants.DEFAULT_WFS_VERSION;
    }

    public String getLanguage() {
        return LANGUAGE;
    }

}
