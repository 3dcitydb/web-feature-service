/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2017
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vcs.citydb.wfs.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.global.Cache;

import vcs.citydb.wfs.config.capabilities.Capabilities;
import vcs.citydb.wfs.config.database.Database;
import vcs.citydb.wfs.config.feature.FeatureTypes;
import vcs.citydb.wfs.config.operation.Operations;
import vcs.citydb.wfs.config.security.Security;
import vcs.citydb.wfs.config.system.Logging;
import vcs.citydb.wfs.config.system.Server;

@XmlRootElement(name="wfs")
@XmlType(name="WFSConfigType", propOrder={
		"capabilities",
		"featureTypes",
		"operations",
		"database",
		"server",
		"uidCache",
		"security",
		"logging"		
})
public class WFSConfig {
	private Capabilities capabilities;
	@XmlElement(required=true)
	private FeatureTypes featureTypes;
	private Operations operations;
	@XmlElement(required=true)
	private Database database;
	@XmlElement(required=true)	
	private Server server;
	private Cache uidCache;
	private Security security;
	private Logging logging;
	
	public WFSConfig() {
		capabilities = new Capabilities();
		featureTypes = new FeatureTypes();
		operations = new Operations();
		database = new Database();
		server = new Server();
		uidCache = new Cache();
		security = new Security();
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

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public Logging getLogging() {
		return logging;
	}

	public void setLogging(Logging logging) {
		this.logging = logging;
	}
	
}