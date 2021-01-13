package vcs.citydb.wfs.config.database;

import org.citydb.config.project.database.DatabaseConnection;
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
	private final DatabaseSrsList referenceSystems;
	@XmlElement(required=true)
	private DatabaseConnection connection;
	
	public Database() {
		referenceSystems = new DatabaseSrsList();
	}

	public List<DatabaseSrs> getReferenceSystems() {
		return referenceSystems.getItems();
	}

	public DatabaseConnection getConnection() {
		return connection;
	}

	public void setConnection(DatabaseConnection connection) {
		this.connection = connection;
	}

}
