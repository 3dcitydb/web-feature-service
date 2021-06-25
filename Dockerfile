# 3DCityDB Web Feature Service Dockerfile #####################################
#   Official website    https://www.3dcitydb.net
#   GitHub              https://github.com/3dcitydb/web-feature-service
###############################################################################

# 3DCityDB Importer/Exporter Dockerfile #######################################
#   Official website    https://www.3dcitydb.net
#   GitHub              https://github.com/3dcitydb/importer-exporter
###############################################################################

# Fetch & build stage #########################################################
# ARGS
ARG BUILDER_IMAGE_TAG='11.0.11-jdk-slim'
ARG RUNTIME_IMAGE_TAG='9-jdk11'

# Base image
FROM openjdk:${BUILDER_IMAGE_TAG} AS builder

# Copy source code
WORKDIR /build_tmp
COPY . ./

# Build
RUN set -x && \
  chmod u+x ./gradlew && ./gradlew installDist

# Move dist to /impexp
RUN set -x && \
  mkdir -p /wfs && \
  mv build/install/3DCityDB-Web-Feature-Service/* /wfs && \
  mv docker-scripts/citydb-wfs.sh /wfs

# Cleanup dist
RUN set -x && \
  rm -rf /wfs/license /wfs/*.md

# Runtime stage ###############################################################
# Base image
FROM tomcat:${RUNTIME_IMAGE_TAG} AS runtime

ARG CITYDB_WFS_CONTEXT_PATH="citydb-wfs"
ENV CITYDB_WFS_CONTEXT_PATH=${CITYDB_WFS_CONTEXT_PATH}

# Install xmlstarlet
RUN set -x && \
  apt-get update && \
  apt-get install -y --no-install-recommends xmlstarlet && \
  rm -rf /var/lib/apt/lists/*

# Run as non-root user
RUN set -x && \
  groupadd --gid 1000 wfs && \
  useradd --uid 1000 --gid 1000 wfs

# copy from builder
WORKDIR /wfs
COPY --chown wfs:wfs --from=builder /wfs .


# Extract WAR to Tomcat apps folder and copy libs
RUN set -x && \
  unzip 'citydb-wfs.war' -d "/usr/local/tomcat/webapps/${CITYDB_WFS_CONTEXT_PATH}" && \
  mv -v lib/*.jar /usr/local/tomcat/lib/ && \
  mv -v citydb-wfs.sh /usr/local/bin/ && \
  chmod -v u+x /usr/local/bin/citydb-wfs.sh && \
  chown -R wfs:wfs /usr/local/tomcat

USER wfs

ENTRYPOINT ["citydb-wfs.sh"]
CMD ["catalina.sh","run"]

# Labels ######################################################################
LABEL maintainer="Bruno Willenborg"
LABEL maintainer.email="b.willenborg(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/3dcitydb/web-feature-service"