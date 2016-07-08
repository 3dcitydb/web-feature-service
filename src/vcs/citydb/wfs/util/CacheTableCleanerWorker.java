/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2016
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
package vcs.citydb.wfs.util;

import java.sql.SQLException;

import org.citydb.api.concurrent.DefaultWorkerImpl;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;

public class CacheTableCleanerWorker extends DefaultWorkerImpl<CacheTableManager> {
	private final Logger log = Logger.getInstance();
	
	@Override
	public void doWork(CacheTableManager work) {
		try {
			work.dropAll();
		} catch (SQLException e) {
			log.error("Failed to clean temporary cache: " + e.getMessage());
		}
	}

	@Override
	public void shutdown() {
		// nothing to do
	}

}
