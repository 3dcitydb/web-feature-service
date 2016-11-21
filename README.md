3DCityDB Web Feature Service Interface
======================================
The OGC Web Feature Service (WFS) interface for the 3D City Database enables web-based access to the city objects stored in the database. WFS clients can directly connect to this standardized and open interface for requesting 3D content across the web using platform-independent calls. Users of the 3D City Database are therefore no longer limited to using the [Importer/Exporter](https://github.com/3dcitydb/importer-exporter) tool for data retrieval. The Web feature services allows clients to only retrieve the city objects they are seeking, rather than retrieving a file that contains the data they are seeking and possibly much more. 

The 3D City Database WFS interface is implemented against the latest version 2.0 of the [OGC Web Feature Service standard](http://www.opengeospatial.org/standards/wfs) and hence is compliant with ISO 19142:2010. Previous versions of the WFS standard are not supported. The implementation currently satisfies the `Simple WFS` conformance class. The development of the WFS is led by the company [virtualcitySYSTEMS](http://www.virtualcitysystems.de/) which offers an extended version of the WFS with additional functionalities that go beyond the Simple WFS class (e.g., thematic and spatial filter capabilities and transaction support). This additional functionality may be fed back to the open source project in future releases.

License
-------
The 3D City Database Web Feature Service is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0). See the `LICENSE` file for more details.

Note that releases of the software before version 3.3.0 continue to be licensed under GNU LGPL 3.0. To request a previous release of the 3D City Database Importer/Exporter under Apache License 2.0 create a GitHub issue.

Latest release
--------------
The latest stable release of the 3D City Database WFS interface is 3.3.1.

Download a WAR file of the WFS [here](https://github.com/3dcitydb/web-feature-service/releases/download/v3.3.1/citydb-wfs-3.3.1.zip). Previous releases are available from the [releases section](https://github.com/3dcitydb/web-feature-service/releases).

System requirements
-------------------

The 3D City Database WFS is implemented as Java web application based on the Java Servlet technology. It therefore must be run in a Java servlet container on a web server. The following minimum software requirements have to be met:

* Java servlet container supporting the Java Servlet 3.1/3.0 specification
* Java 8 Runtime Environment (Java 7 or earlier versions are not supported)  

The WFS implementation has been successfully deployed and tested on [Apache Tomcat](http://tomcat.apache.org/) versions 8 and 7. 

Documentation
-------------
A complete and comprehensive documentation on the Web Feature Service is installed with the [3D City Database Importer/Exporter](https://github.com/3dcitydb/importer-exporter) and is available [online](http://www.3dcitydb.org/3dcitydb/documentation/).

Contributing
------------
* To file bugs found in the software create a GitHub issue.
* To contribute code for fixing filed issues create a pull request with the issue id.
* To propose a new feature create a GitHub issue and open a discussion.

Developers
----------

The 3D City Database Web Feature Service has been developed by: 

* Claus Nagel 
<br>[virtualcitySYSTEMS GmbH, Berlin](http://www.virtualcitysystems.de/)
