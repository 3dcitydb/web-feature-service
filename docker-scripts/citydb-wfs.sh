#!/usr/bin/env bash
# 3DCityDB WFS Docker ENTRYPONT ###############################################

# Print commands and their arguments as they are executed
set -e;

# Update tomcat opts ##########################################################
if [ -z ${TOMCAT_MAX_HEAP+x} ]; then
  export TOMCAT_MAX_HEAP=1024m;
fi

if [ -z ${CATALINA_OPTS+x} ]; then
  export CATALINA_OPTS="-Djava.awt.headless=true
    -Dfile.encoding=UTF-8
    -server -Xms$(awk '/MemTotal/ { printf "%d\n", $2/1024/1024/2}' /proc/meminfo)g
    -Xmx$(awk '/MemTotal/ { printf "%d\n", $2/1024/1024}' /proc/meminfo)g"
fi

# Set default env #############################################################
echo
echo "# Setting up 3DCityDB WFS environment ... ######################################"
if [ -z ${CITYDB_CONNECTION_TYPE+x} ]; then
  export CITYDB_CONNECTION_TYPE=PostGIS;
  echo "NOTE: Environment variable not set, using default value:"
  echo "   CITYDB_CONNECTION_TYPE=$CITYDB_CONNECTION_TYPE"
  echo "   To change this setting run e.g.:"
  echo "     docker run -e \"CITYDB_CONNECTION_TYPE=Oracle\" 3dcitydb/wfs"
fi

if [ -z ${CITYDB_CONNECTION_HOST+x} ]; then
  export CITYDB_CONNECTION_HOST=localhost;
  echo
  echo "NOTE: Environment variable not set, using default value:"
  echo "   CITYDB_CONNECTION_HOST=$CITYDB_CONNECTION_HOST"
  echo "   To change this setting run e.g.:"
  echo "     docker run -e \"CITYDB_CONNECTION_HOST=my.other.host\" 3dcitydb/wfs"
fi

if [ -z ${CITYDB_CONNECTION_PORT+x} ]; then
  export CITYDB_CONNECTION_PORT=5432;
  echo
  echo "NOTE: Environment variable not set, using default value:"
  echo "   CITYDB_CONNECTION_PORT=$CITYDB_CONNECTION_PORT"
  echo "   To change this setting run e.g.:"
  echo "     docker run -e \"CITYDB_CONNECTION_PORT=1234\" 3dcitydb/wfs"
fi

if [ -z ${CITYDB_CONNECTION_DBNAME+x} ]; then
  export CITYDB_CONNECTION_DBNAME="citydb";
  echo
  echo "NOTE: Environment variable not set, using default value:"
  echo "   CITYDB_CONNECTION_DBNAME=$CITYDB_CONNECTION_DBNAME"
  echo "   To change this setting run e.g.:"
  echo "     docker run -e \"CITYDB_CONNECTION_DBNAME=citydb\" 3dcitydb/wfs"
fi

if [ -z ${CITYDB_CONNECTION_USER+x} ]; then
  export CITYDB_CONNECTION_USER="postgres";
  echo
  echo "NOTE: Environment variable not set, using default value:"
  echo "   CITYDB_CONNECTION_USER=$CITYDB_CONNECTION_USER"
  echo "   To change this setting run e.g.:"
  echo "     docker run -e \"CITYDB_CONNECTION_USER=otheruser\" 3dcitydb/wfs"
fi

if [ -z ${CITYDB_CONNECTION_PASSWORD+x} ]; then
  export CITYDB_CONNECTION_PASSWORD="postgres";
  echo
  echo "NOTE: Environment variable not set, using default value:"
  echo "   CITYDB_CONNECTION_PASSWORD=$CITYDB_CONNECTION_PASSWORD"
  echo "   To change this setting run e.g.:"
  echo "     docker run -e \"CITYDB_CONNECTION_PASSWORD=changeMe!!\" 3dcitydb/wfs"
fi
echo
echo "# Setting up 3DCityDB WFS environment ...done! #################################"

# Insert database credentials into 3dcitydb-wfs WEB-INF/config.xml ############
echo
echo "# Writing 3DCityDB WFS WEB-INF/config.xml ... ##################################"
xmlstarlet ed -L -N n="http://www.3dcitydb.org/importer-exporter/config" \
 -u "/n:wfs/n:database/n:connection/n:type" -v "$CITYDB_CONNECTION_TYPE" "/usr/local/tomcat/webapps/${CITYDB_WFS_CONTEXT_PATH}/WEB-INF/config.xml"
xmlstarlet ed -L -N n="http://www.3dcitydb.org/importer-exporter/config" \
 -u "/n:wfs/n:database/n:connection/n:server" -v "$CITYDB_CONNECTION_HOST" "/usr/local/tomcat/webapps/${CITYDB_WFS_CONTEXT_PATH}/WEB-INF/config.xml"
xmlstarlet ed -L -N n="http://www.3dcitydb.org/importer-exporter/config" \
 -u "/n:wfs/n:database/n:connection/n:port" -v "$CITYDB_CONNECTION_PORT" "/usr/local/tomcat/webapps/${CITYDB_WFS_CONTEXT_PATH}/WEB-INF/config.xml"
xmlstarlet ed -L -N n="http://www.3dcitydb.org/importer-exporter/config" \
 -u "/n:wfs/n:database/n:connection/n:sid" -v "$CITYDB_CONNECTION_DBNAME" "/usr/local/tomcat/webapps/${CITYDB_WFS_CONTEXT_PATH}/WEB-INF/config.xml"
xmlstarlet ed -L -N n="http://www.3dcitydb.org/importer-exporter/config" \
 -u "/n:wfs/n:database/n:connection/n:user" -v "$CITYDB_CONNECTION_USER" "/usr/local/tomcat/webapps/${CITYDB_WFS_CONTEXT_PATH}/WEB-INF/config.xml"
xmlstarlet ed -L -N n="http://www.3dcitydb.org/importer-exporter/config" \
 -u "/n:wfs/n:database/n:connection/n:password" -v "$CITYDB_CONNECTION_PASSWORD" "/usr/local/tomcat/webapps/${CITYDB_WFS_CONTEXT_PATH}/WEB-INF/config.xml"

echo "# Writing 3DCityDB WFS WEB-INF/config.xml ...done! #############################"

# Switch to catalina.sh run
exec "$@"
