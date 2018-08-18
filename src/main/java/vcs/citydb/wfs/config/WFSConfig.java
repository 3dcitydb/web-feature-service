package vcs.citydb.wfs.config;

import org.citydb.config.project.global.Cache;
import vcs.citydb.wfs.config.capabilities.Capabilities;
import vcs.citydb.wfs.config.constraints.Constraints;
import vcs.citydb.wfs.config.database.Database;
import vcs.citydb.wfs.config.feature.FeatureTypes;
import vcs.citydb.wfs.config.operation.Operations;
import vcs.citydb.wfs.config.processing.PostProcessing;
import vcs.citydb.wfs.config.system.Logging;
import vcs.citydb.wfs.config.system.Server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="wfs")
@XmlType(name="WFSConfigType", propOrder={
		"capabilities",
		"featureTypes",
		"operations",
		"postProcessing",
		"database",
		"server",
		"uidCache",
		"constraints",
		"logging"		
})
public class WFSConfig {
	@XmlElement(required=true)
	private Capabilities capabilities;
	@XmlElement(required=true)
	private FeatureTypes featureTypes;
	private Operations operations;
	private PostProcessing postProcessing;
	@XmlElement(required=true)
	private Database database;
	@XmlElement(required=true)	
	private Server server;
	private Cache uidCache;
	private Constraints constraints;
	private Logging logging;
	
	public WFSConfig() {
		capabilities = new Capabilities();
		featureTypes = new FeatureTypes();
		operations = new Operations();
		postProcessing = new PostProcessing();
		database = new Database();
		server = new Server();
		uidCache = new Cache();
		constraints = new Constraints();
		logging = new Logging();
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public Cache getUidCache() {
		return uidCache;
	}

	public void setUidCache(Cache uidCache) {
		this.uidCache = uidCache;
	}

	public FeatureTypes getFeatureTypes() {
		return featureTypes;
	}

	public void setFeatureTypes(FeatureTypes featureTypes) {
		this.featureTypes = featureTypes;
	}

	public Operations getOperations() {
		return operations;
	}

	public void setOperations(Operations operations) {
		this.operations = operations;
	}

	public PostProcessing getPostProcessing() {
		return postProcessing;
	}

	public void setPostProcessing(PostProcessing postProcessing) {
		this.postProcessing = postProcessing;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Cache getUIDCache() {
		return uidCache;
	}

	public void setUIDCache(Cache uidCache) {
		this.uidCache = uidCache;
	}

	public Constraints getConstraints() {
		return constraints;
	}

	public void setConstraints(Constraints constraints) {
		this.constraints = constraints;
	}

	public Logging getLogging() {
		return logging;
	}

	public void setLogging(Logging logging) {
		this.logging = logging;
	}
	
}