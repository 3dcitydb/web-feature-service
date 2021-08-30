#!/usr/bin/env bash
# 3DCityDB WFS Docker ENTRYPONT ###############################################

# Print commands and their arguments as they are executed
set -e;

# Set default tomcat opts #####################################################
if [ -z ${CATALINA_OPTS+x} ]; then
  export CATALINA_OPTS="-Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Xms$(awk '/MemTotal/ { printf "%d\n", $2/1024/1024/2}' /proc/meminfo)G \
    -Xmx$(awk '/MemTotal/ { printf "%d\n", $2/1024/1024}' /proc/meminfo)G"
fi

exec "$@"
