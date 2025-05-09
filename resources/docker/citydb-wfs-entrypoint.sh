#!/usr/bin/env bash
# 3DCityDB WFS Docker ENTRYPOINT ##############################################

# Print commands and their arguments as they are executed
set -e;

# Set default tomcat opts #####################################################
if [ -z ${CATALINA_OPTS+x} ]; then
  export CATALINA_OPTS="-Djava.awt.headless=true \
    -Dfile.encoding=UTF-8"
fi

# Adapt context path of the WFS service #######################################
if [ ! -z ${WFS_CONTEXT_PATH} ]; then
  mv ${CATALINA_HOME}/webapps/ROOT.war ${CATALINA_HOME}/webapps/${WFS_CONTEXT_PATH}.war
fi

exec "$@"