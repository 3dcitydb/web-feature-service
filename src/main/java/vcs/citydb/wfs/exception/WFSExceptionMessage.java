package vcs.citydb.wfs.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WFSExceptionMessage {
    private final WFSExceptionCode exceptionCode;
    private List<String> exceptionTexts = new ArrayList<>();
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
        if (exceptionText != null)
            exceptionTexts.add(exceptionText);
    }

    public void addExceptionTexts(Collection<String> exceptionTexts) {
        if (exceptionTexts != null)
            exceptionTexts.forEach(this::addExceptionText);
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

}
