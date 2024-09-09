3DCityDB Web Feature Service Interface
======================================
The OGC Web Feature Service (WFS) interface for the 3D City Database enables web-based access to the city objects stored
in the database. WFS clients can directly connect to this standardized and open interface for requesting 3D content
across the web using platform-independent calls. Users of the 3D City Database are therefore no longer limited to using
the [Importer/Exporter](https://github.com/3dcitydb/importer-exporter) tool for data retrieval. The Web Feature Service
allows clients to only retrieve the city objects they are seeking, rather than retrieving a file that contains the data
they are seeking and possibly much more.

The 3D City Database WFS interface is implemented against the latest version 2.0 of the [OGC Web Feature Service standard](http://www.opengeospatial.org/standards/wfs)
and hence is compliant with ISO 19142:2010. Previous versions of the WFS standard are not supported.
The development of the WFS is led by the company [Virtual City Systems](https://vc.systems/)
that offers an extended version of the WFS with additional capabilities such as, for instance,
transaction support through insert, update, replace and delete operations. This additional functionality may
be fed back to the open source project in future releases.

License
-------
The 3D City Database Web Feature Service is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
See the `LICENSE` file for more details.

Note that releases of the software before version 3.3.0 continue to be licensed under GNU LGPL 3.0. To request a
previous release of the 3D City Database Importer/Exporter under Apache License 2.0 create a GitHub issue.

Latest release
--------------
The latest stable release of the 3D City Database WFS interface is 5.3.2.

Download a ZIP file of the WFS [here](https://github.com/3dcitydb/web-feature-service/releases/download/v5.3.2/3DCityDB-Web-Feature-Service-5.3.2.zip).
Previous releases are available from the [releases section](https://github.com/3dcitydb/web-feature-service/releases).

System requirements
-------------------
The 3D City Database WFS is implemented as Java web application based on the Java Servlet technology. It therefore must
be run in a Java servlet container on a web server. The following minimum software requirements have to be met:

* Java servlet container supporting the Java Servlet 3.1/3.0 specification
* Java 8 Runtime Environment (or higher)

The WFS implementation has been successfully deployed and tested on [Apache Tomcat](http://tomcat.apache.org/)
versions 9 and 8. All previous versions of the Apache Tomcat server have reached end of life and are not supported
anymore.

Documentation
-------------
A complete and comprehensive user manual on the Web Feature Service is available
[online](https://3dcitydb-docs.readthedocs.io/en/version-2023.0/wfs/).

The documentation contains a step-by-step guide for deploying the WFS on a servlet container.

Contributing
------------
* To file bugs found in the software create a GitHub issue.
* To contribute code for fixing filed issues create a pull request with the issue id.
* To propose a new feature create a GitHub issue and open a discussion.

Building
--------
The project uses [Gradle](https://gradle.org/) as build system. To build the WFS from source, clone the repository to
your local machine and run the following command from the root of the repository.

    > gradlew installDist
    
The build process will produce the WFS software package under `build/install`.

Using with Docker
-----------------

The 3D City Database Web Feature Service is also available as Docker image. You can either build the image
yourself using one of the provided Docker files or use a pre-built image from Docker Hub at
https://hub.docker.com/r/3dcitydb/wfs.

To build the image, clone the repository to your local machine and run the following command from the root of the
repository:

    > docker build -t 3dcitydb/wfs .

Using the Docker image of the 3D City Database WFS is simple:

```
> docker run --name wfs [-d] -p 8080:8080 \
    [-e CITYDB_TYPE=postgresql|oracle] \
    [-e CITYDB_HOST=the.host.de] \
    [-e CITYDB_PORT=thePort] \
    [-e CITYDB_NAME=theDBName] \
    [-e CITYDB_SCHEMA=theCityDBSchemaName] \
    [-e CITYDB_USERNAME=theUsername] \
    [-e CITYDB_PASSWORD=theSecretPass] \
    [-e WFS_CONTEXT_PATH=wfs-context-path] \
    [-e WFS_ADE_EXTENSIONS_PATH=/path/to/ade-extensions/] \
    [-e WFS_CONFIG_FILE=/path/to/config.xml] \
    [-v /my/data/config.xml:/path/to/config.xml] \
  3dcitydb/wfs
```

When running a Docker container with default settings, the WFS will listen at the following URL.

    http[s]://[host][:port]/wfs

More details on how to use the 3D City Database WFS with Docker can be found in the
[online documentation](https://3dcitydb-docs.readthedocs.io/en/version-2023.0/wfs/).

Developers
----------

The 3D City Database Web Feature Service has been developed by: 

* [Virtual City Systems, Berlin](https://vc.systems/)