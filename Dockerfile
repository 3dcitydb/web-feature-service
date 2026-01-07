# 3DCityDB Web Feature Service Dockerfile #####################################
#   Official website    https://www.3dcitydb.org
#   GitHub              https://github.com/3dcitydb/web-feature-service
###############################################################################

# Fetch & build stage #########################################################
# ARGS
ARG BUILDER_IMAGE_TAG='21-jdk-noble'
ARG RUNTIME_IMAGE_TAG='9-jdk21-temurin-noble'

# Base image
FROM eclipse-temurin:${BUILDER_IMAGE_TAG} AS builder

ARG DEFAULT_CONFIG='default-config.xml'

# Copy source code
WORKDIR /build
COPY . /build

# Copy default config
COPY resources/docker/${DEFAULT_CONFIG} src/main/webapp/WEB-INF/config.xml

# Build
RUN chmod u+x ./gradlew && ./gradlew installDist

# Runtime stage ###############################################################
# Base image
FROM tomcat:${RUNTIME_IMAGE_TAG} AS runtime

# Prepare working directory for non-root user
RUN mkdir -p /citydb-wfs && \
    chown -R 1000:1000 "$CATALINA_HOME" /citydb-wfs && \
    rm -rf ${CATALINA_HOME}/webapps/ROOT

# Copy WAR to webapps folder
COPY --from=builder --chown=1000:1000 /build/build/install/3DCityDB-Web-Feature-Service/citydb-wfs.war \
     ${CATALINA_HOME}/webapps/ROOT.war

COPY --chown=1000:1000 resources/docker/citydb-wfs-entrypoint.sh /usr/local/bin/

# Delete existing ROOT context and set permissions
RUN chmod a+x /usr/local/bin/citydb-wfs-entrypoint.sh

WORKDIR /citydb-wfs
USER 1000
EXPOSE 8080

ENTRYPOINT ["citydb-wfs-entrypoint.sh"]
CMD ["catalina.sh","run"]