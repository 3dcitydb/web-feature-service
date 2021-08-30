# 3DCityDB Web Feature Service Dockerfile #####################################
#   Official website    https://www.3dcitydb.org
#   GitHub              https://github.com/3dcitydb/web-feature-service
###############################################################################

# Fetch & build stage #########################################################
# ARGS
ARG BUILDER_IMAGE_TAG='11.0.12-jdk-slim'
ARG RUNTIME_IMAGE_TAG='9-jdk11'

# Base image
FROM openjdk:${BUILDER_IMAGE_TAG} AS builder

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

ARG CONTEXT_PATH='ROOT'
ARG TOMCAT_USER='tomcat'
ARG TOMCAT_GROUP='tomcat'

# Run as non-root user user
RUN groupadd --gid 1000 -r ${TOMCAT_GROUP} && \
    useradd --uid 1000 --gid 1000 -d /citydb-wfs -m -r --no-log-init ${TOMCAT_USER} && \
    chown -R 1000:1000 ${CATALINA_HOME}

# Copy WAR to webapps folder
COPY --from=builder --chown=1000:1000 /build/build/install/3DCityDB-Web-Feature-Service/citydb-wfs.war \
       ${CATALINA_HOME}/webapps/${CONTEXT_PATH}.war

COPY --chown=1000:1000 resources/docker/citydb-wfs-entrypoint.sh /usr/local/bin/

# Set permissions
RUN chmod a+x /usr/local/bin/citydb-wfs-entrypoint.sh

WORKDIR /citydb-wfs
USER 1000
EXPOSE 8080

ENTRYPOINT ["citydb-wfs-entrypoint.sh"]
CMD ["catalina.sh","run"]

# Labels ######################################################################
LABEL maintainer="Bruno Willenborg"
LABEL maintainer.email="b.willenborg(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/3dcitydb/web-feature-service"