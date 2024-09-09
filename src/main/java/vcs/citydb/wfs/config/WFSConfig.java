package vcs.citydb.wfs.config;

import vcs.citydb.wfs.config.capabilities.Capabilities;
import vcs.citydb.wfs.config.constraints.Constraints;
import vcs.citydb.wfs.config.database.Database;
import vcs.citydb.wfs.config.feature.FeatureTypes;
import vcs.citydb.wfs.config.filter.FilterCapabilities;
import vcs.citydb.wfs.config.logging.Logging;
import vcs.citydb.wfs.config.operation.Operations;
import vcs.citydb.wfs.config.processing.PostProcessing;
import vcs.citydb.wfs.config.server.Server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "wfs")
@XmlType(name = "WFSConfigType", propOrder = {
        "capabilities",
        "featureTypes",
        "operations",
        "filterCapabilities",
        "constraints",
        "postProcessing",
        "database",
        "server",
        "logging"
})
public class WFSConfig {
    @XmlElement(required = true)
    private Capabilities capabilities;
    @XmlElement(required = true)
    private FeatureTypes featureTypes;
    private Operations operations;
    private FilterCapabilities filterCapabilities;
    private Constraints constraints;
    private PostProcessing postProcessing;
    @XmlElement(required = true)
    private Database database;
    @XmlElement(required = true)
    private Server server;
    private Logging logging;

    public WFSConfig() {
        capabilities = new Capabilities();
        featureTypes = new FeatureTypes();
        operations = new Operations();
        filterCapabilities = new FilterCapabilities();
        constraints = new Constraints();
        postProcessing = new PostProcessing();
        database = new Database();
        server = new Server();
        logging = new Logging();
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
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

    public FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }

    public void setFilterCapabilities(FilterCapabilities filterCapabilities) {
        this.filterCapabilities = filterCapabilities;
    }

    public Constraints getConstraints() {
        return constraints;
    }

    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
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

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

}