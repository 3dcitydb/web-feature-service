/*
 * This file is part of the 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright (c) 2014
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 * 
 * The 3D City Database Web Feature Service is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package vcs.citydb.wfs.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.database.Database;
import org.citydb.config.project.global.Cache;

import vcs.citydb.wfs.config.capabilities.Capabilities;
import vcs.citydb.wfs.config.feature.FeatureTypes;
import vcs.citydb.wfs.config.operation.Operations;
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
	private Logging logging;
	
	public WFSConfig() {
		capabilities = new Capabilities();
		featureTypes = new FeatureTypes();
		operations = new Operations();
		database = new Database();
		server = new Server();
		uidCache = new Cache();
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

	public Logging getLogging() {
		return logging;
	}

	public void setLogging(Logging logging) {
		this.logging = logging;
	}
	
}