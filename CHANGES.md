Change Log
==========

### 4.3.0

##### Changes
* Improved export performance (up to 10-15 times faster in case the WFS is not running on the same machine or
  in the same local network as the database server).
* The default `config.xml` file and ADE extensions folder can now be changed using the environment variables
  `VC_WFS_CONFIG_FILE` and `VC_WFS_ADE_EXTENSIONS_PATH`.
* Enhanced LoD filter constraint with the option to only export the minimum or maximum LoD from the list of
  selected LoDs.
* Updated WFS web client UI and added support for XML highlighting to the input and output fields.
* Added `countDefault` constraint to limit the maximum number of returned features.
* Added CityJSON output format option `removeDuplicateChildGeometries` to avoid exporting duplicate geometries
  for nested child objects.
* This release is based on the Importer/Exporter version 4.3.0, and thus incorporates all bug fixes and updates
  introduced in that version.

##### Fixes
* Fixed error when exporting to CityJSON and no top-level feature is returned.

##### Miscellaneous
* Updated citygml4j to 2.11.3.
* Upgrade PostgreSQL driver to 42.2.14 and PostGIS to 2.5.0.

### 4.2.3 - 2020-07-16

##### Changes
* CityJSON output now uses the latest CityJSON version 1.0.1.
* This release is based on the Importer/Exporter version 4.2.3, and thus incorporates all bug fixes and updates introduced
in that version ([more information](https://github.com/3dcitydb/importer-exporter/releases/tag/v4.2.3)).

##### Miscellaneous
* Updated citygml4j to 2.10.5.
* Upgrade to latest PostgreSQL driver 42.2.10
* Upgrade Oracle driver to 19.3.

### 4.2 - 2019-02-13

##### Changes
* CityJSON output now uses the latest CityJSON version 0.9.

##### Fixes
* Using UTF-8 encoding for CityJSON output per default.
* This release is based on the Importer/Exporter version 4.2.0, and thus incorporates all bug fixes and updates introduced 
in that version ([more information](https://github.com/3dcitydb/importer-exporter/releases/tag/v4.2.0)). 

##### Miscellaneous 
* Updated citygml4j to 2.9.1.

### 4.1 - 2019-01-09

##### Changes
* renamed the parameter `<useCityDBADE>` to `<exportCityDBMetadata>` in the `config.xml` file. This parameter controls
whether metadata such as the `LINEAGE` attribute of the `CITYOBJECT` table should be written to the response document.
If set to `true`, the 3DCityDB ADE will be used for storing the information with the city objects.

##### Fixes
* This release is based on the Importer/Exporter version 4.1.0, and thus incorporates all bug fixes and updates
introduced in that version ([more information](https://github.com/3dcitydb/importer-exporter/releases/tag/v4.1.0)).

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
* [3DCityDB Docker images](https://github.com/tum-gis/3dcitydb-docker-postgis) are now available for a range of 3DCityDB
and WFS versions to support continuous integration workflows.
