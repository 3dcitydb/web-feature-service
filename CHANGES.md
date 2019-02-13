Change Log
==========

### 4.2 - 2019-02-13

##### Changes
* CityJSON output now uses the latest CityJSON version 0.9.

##### Fixes
* Encoding of CityJSON output is UTF-8 per default.
* This release is based on the Importer/Exporter version 4.2.0, and thus incorporates all bug fixes and updates introduced 
in that version ([more information](https://github.com/3dcitydb/importer-exporter/releases/tag/v4.2.0)). 

##### Miscellaneous 
* Updated citygml4j to 2.9.1.

### 4.1 - 2019-01-09

##### Changes
* renamed the parameter `<useCityDBADE>` to `<exportCityDBMetadata>` in the `config.xml` file. This parameter controls whether metadata such as the `LINEAGE` attribute of the `CITYOBJECT` table should be written to the response document. If set to `true`, the 3DCityDB ADE will be used for storing the information with the city objects. 

##### Fixes
* This release is based on the Importer/Exporter version 4.1.0, and thus incorporates all bug fixes and updates introduced in that version ([more information](https://github.com/3dcitydb/importer-exporter/releases/tag/v4.1.0)). 

##### Miscellaneous 
* Upgrade to latest PostgreSQL driver v42.2.5 and PostGIS driver v2.3.0.
* Upgrade to latest Oracle driver 18.3.

### 4.0 - 2018-09-18

##### Additions
* Added support for CityGML ADEs through ADE extensions.
* Added KVP over HTTP GET as additional service binding to simplify the integration with GIS and ETL software such as FME.
* Added [CityJSON](http://www.cityjson.org/) as additional output format besides CityGML.
* New LoD filter for WFS responses.
* Added support for CORS.
* Database connections are not established at service startup but lazily when required.
* Major update to Importer/Exporter library 4.0.
* Switched from Ant to Gradle as build system.

##### Miscellaneous 
* [3DCityDB Docker images](https://github.com/tum-gis/3dcitydb-docker-postgis) are now available for a range of 3DCityDB and WFS versions to support continuous integration workflows.
