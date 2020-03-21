package vcs.citydb.wfs.config.database;

import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.DatabaseSrsList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(name="WFSDatabaseType", propOrder={
		"referenceSystems",
		"connection"
})
public class Database {
	private DatabaseSrsList referenceSystems;
	@XmlElement(required=true)
	private DBConnection connection;
	
	public Database() {
		referenceSystems = new DatabaseSrsList();
	}

	public List<DatabaseSrs> getReferenceSystems() {
		return referenceSystems.getItems();
	}

	public DBConnection getConnection() {
		return connection;
	}

	public void setConnection(DBConnection connection) {
		this.connection = connection;
	}

}
