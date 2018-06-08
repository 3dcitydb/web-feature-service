package vcs.citydb.wfs.config.system;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="LoggingType", propOrder={
		"console",
		"file"
})
public class Logging {
	private FileLog file;
	private ConsoleLog console;
	
	public Logging() {
		file = new FileLog();
	}
	
	public ConsoleLog getConsole() {
		return console;
	}
	
	public void setConsole(ConsoleLog console) {
		this.console = console;
	}
	
	public FileLog getFile() {
		return file;
	}
	
	public void setFile(FileLog file) {
		this.file = file;
	}
	
}
