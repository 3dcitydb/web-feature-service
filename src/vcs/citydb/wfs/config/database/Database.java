package vcs.citydb.wfs.config.database;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.database.DBConnection;

@XmlType(name="WFSDatabaseType", propOrder={
		"connection"
})
public class Database {
	@XmlElement(required=true)
	private DBConnection connection;

	public DBConnection getConnection() {
		return connection;
	}

}
