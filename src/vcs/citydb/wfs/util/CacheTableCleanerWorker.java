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
