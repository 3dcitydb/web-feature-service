Change Log
==========

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