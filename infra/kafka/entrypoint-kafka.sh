#!/usr/bin/env bash
set -euo pipefail

KAFKA_HOME=${KAFKA_HOME:-/opt/kafka}
CONFIG_FILE="${KAFKA_HOME}/config/server.properties"

BROKER_ID=${BROKER_ID:-0}
ZOOKEEPER_CONNECT=${ZOOKEEPER_CONNECT:-zookeeper:2181}
LISTENERS=${LISTENERS:-PLAINTEXT://0.0.0.0:9092}
ADVERTISED_LISTENERS=${ADVERTISED_LISTENERS:-PLAINTEXT://localhost:9092}
LOG_DIRS=${LOG_DIRS:-/var/lib/kafka/data}

cp "${KAFKA_HOME}/config/server.properties" "${CONFIG_FILE}.work"

sed -i "s|^broker.id=.*|broker.id=${BROKER_ID}|g" "${CONFIG_FILE}.work"
sed -i "s|^zookeeper.connect=.*|zookeeper.connect=${ZOOKEEPER_CONNECT}|g" "${CONFIG_FILE}.work"

sed -i '/^listeners=/d' "${CONFIG_FILE}.work"
sed -i '/^advertised.listeners=/d' "${CONFIG_FILE}.work"
echo "listeners=${LISTENERS}" >> "${CONFIG_FILE}.work"
echo "advertised.listeners=${ADVERTISED_LISTENERS}" >> "${CONFIG_FILE}.work"

sed -i "s|^log.dirs=.*|log.dirs=${LOG_DIRS}|g" "${CONFIG_FILE}.work"

mkdir -p "${LOG_DIRS}"

exec "${KAFKA_HOME}/bin/kafka-server-start.sh" "${CONFIG_FILE}.work"
