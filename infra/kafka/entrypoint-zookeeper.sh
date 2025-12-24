#!/usr/bin/env bash
set -euo pipefail

KAFKA_HOME=${KAFKA_HOME:-/opt/kafka}
CONFIG_FILE="${KAFKA_HOME}/config/zookeeper.properties"

DATA_DIR=${DATA_DIR:-/var/lib/zookeeper/data}
DATA_LOG_DIR=${DATA_LOG_DIR:-/var/lib/zookeeper/log}
CLIENT_PORT=${CLIENT_PORT:-2181}

cp "${KAFKA_HOME}/config/zookeeper.properties" "${CONFIG_FILE}.work"

sed -i "s|^dataDir=.*|dataDir=${DATA_DIR}|g" "${CONFIG_FILE}.work"
sed -i "s|^dataLogDir=.*|dataLogDir=${DATA_LOG_DIR}|g" "${CONFIG_FILE}.work"
sed -i "s|^clientPort=.*|clientPort=${CLIENT_PORT}|g" "${CONFIG_FILE}.work"

mkdir -p "${DATA_DIR}" "${DATA_LOG_DIR}"

exec "${KAFKA_HOME}/bin/zookeeper-server-start.sh" "${CONFIG_FILE}.work"
