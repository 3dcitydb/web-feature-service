package vcs.citydb.wfs.config.system;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.global.LogLevel;

@XmlType(name="ConsoleLogType", propOrder={
		"logLevel"
})
public class ConsoleLog {
	@XmlAttribute(required=true)
	private LogLevel logLevel = LogLevel.INFO;

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
}
