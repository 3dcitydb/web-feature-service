Change Log
==========

### 5.2.0 - 2022-05-23

This release is based on the Importer/Exporter version 5.2.0 libraries, and thus incorporates all bug fixes and updates
introduced in that version ([more information](https://github.com/3dcitydb/importer-exporter/releases/tag/v5.2.0)).

##### Additions
* Added support for 3DCityDB v4.3
* Added `replaceResourceIds` configuration option to replace all identifiers of features and geometries with UUIDs.

##### Fixes
* The database port was not correctly set when using the `CITYDB_PORT` environment variable.

##### Miscellaneous
* Updated PostgreSQL driver to 42.3.4
* Updated Oracle driver to 21.3.0.0
* Updated GeoTools to 26.4.

### 5.1.0 - 2022-04-12

This release is based on the Importer/Exporter version 5.1.1 libraries, and thus incorporates all bug fixes and updates
introduced in that version ([more information](https://github.com/3dcitydb/importer-exporter/releases/tag/v5.1.1)).

##### Changes
* Changed the handling of date-time values and timezones. Before this change, `xsd:date` values in exports could be
  different from the values stored in the database in case different timezone settings were used for the database
  server and the import and export operations.
* Simplified database queries used by the `GetFeature` and `GetPropertyValue` operations for top-level features
  involving many nested features/tables to avoid extremely large result sets causing performance issues.

##### Fixes
* Fixed bug in parsing of XPath expressions of XML queries that caused a slash `/` being part of a literal value to
  be interpreted as step operator.
* Empty strings for gml:id attributes are no longer imported but the `GMLID` column is set to `NULL` instead.

##### Miscellaneous
* Updated H2 database used for local caching to 2.1.210.
* Updated PostgreSQL driver to 42.3.1 and PostGIS driver to 2021.1.0.
* Updated citygml4j to 2.12.0.

### 5.0.0 - 2021-10-08

##### Changes
* Added Docker files to build your own images for the WFS service. Pre-built Docker images
  are available from Docker Hub at https://hub.docker.com/r/3dcitydb/wfs. [#9](https://github.com/3dcitydb/web-feature-service/pull/9)
* Support for the WFS 2.0 operations `GetFeature`, `GetPropertyValue`. `CreateStoredQuery`, `DropStoredQuery`
  with both XML and KVP encodings.
* Support for ad-hoc queries and stored queries.
* New filter capabilities including spatial, thematic and logical filters based on OGC Filter Encoding.
* Support for XPath expressions in filter expressions to allow queries on complex attributes and nested features.
* Support for exporting local and global appearances of the requested city objects. Texture images are provided
  through a separate RESTful service that is included in the WFS package and automatically started with the WFS
  service.
* Support for response paging allowing a client to scroll through large sets of features or property values
  based on the `count` and `startIndex` parameters.
* The city objects and property values in a response document can now be sorted by thematic attributes.
* Address attributes and 3DCityDB metadata can be used in query expressions based on corresponding CityGML ADEs
  like with the Importer/Exporter.
* Management of stored queries is now possible.
* Individual WFS operations can now be secured using IP- and token-based access control rules.
* Support for setting the time zone to be used for queries involving date and time attributes.
* JDBC drivers are now kept in `WEB-INF/lib` to better support running the WFS in a Docker environment. So, there
  is no need to copy JDBC drivers to a global or shared lib folder anymore.
* This release is based on the Importer/Exporter version 5.0.0, and thus incorporates all bug fixes and updates
  introduced in that version.
* Added support for providing database connection details via environment variables.
* CityJSON output now uses the latest CityJSON version 1.0.3.
* Many bugfixes and improvements.

##### Miscellaneous
* Updated citygml4j to 2.11.4.
* Updated PostgreSQL driver to 42.2.23 and Oracle driver to 21.1.0.


### 4.3.0 - 2021-04-28

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
