package vcs.citydb.wfs.config.system;

import org.citydb.config.project.global.LogLevel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FileLogType", propOrder={
		"logLevel",
		"fileName"
})
public class FileLog {
	private String fileName;
	@XmlAttribute(required=true)
	private LogLevel logLevel = LogLevel.INFO;

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
