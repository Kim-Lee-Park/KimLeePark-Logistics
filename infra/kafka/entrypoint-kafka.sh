#!/usr/bin/env bash
set -euo pipefail

KAFKA_HOME=${KAFKA_HOME:-/opt/kafka}
CONFIG_FILE="${KAFKA_HOME}/config/kraft/server.properties"

NODE_ID=${NODE_ID:-1}
CLUSTER_ID=${CLUSTER_ID:-$(${KAFKA_HOME}/bin/kafka-storage.sh random-uuid)}
CONTROLLER_QUORUM_VOTERS=${CONTROLLER_QUORUM_VOTERS:-"1@localhost:9093"}
LISTENERS=${LISTENERS:-"PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093"}
ADVERTISED_LISTENERS=${ADVERTISED_LISTENERS:-"PLAINTEXT://localhost:9092"}
LOG_DIRS=${LOG_DIRS:-/var/lib/kafka/data}

cp "${CONFIG_FILE}" "${CONFIG_FILE}.work"

sed -i "s|^node.id=.*|node.id=${NODE_ID}|g" "${CONFIG_FILE}.work"
sed -i "s|^controller.quorum.voters=.*|controller.quorum.voters=${CONTROLLER_QUORUM_VOTERS}|g" "${CONFIG_FILE}.work"

sed -i '/^listeners=/d' "${CONFIG_FILE}.work"
sed -i '/^advertised.listeners=/d' "${CONFIG_FILE}.work"
echo "listeners=${LISTENERS}" >> "${CONFIG_FILE}.work"
echo "advertised.listeners=${ADVERTISED_LISTENERS}" >> "${CONFIG_FILE}.work"

sed -i "s|^log.dirs=.*|log.dirs=${LOG_DIRS}|g" "${CONFIG_FILE}.work"

mkdir -p "${LOG_DIRS}"

if [ ! -f "${LOG_DIRS}/meta.properties" ]; then
  "${KAFKA_HOME}/bin/kafka-storage.sh" format -t "${CLUSTER_ID}" -c "${CONFIG_FILE}.work"
fi

exec "${KAFKA_HOME}/bin/kafka-server-start.sh" "${CONFIG_FILE}.work"
