#!/usr/bin/env bash
set -eo pipefail

# Usage: ./infra/kafka/build_kafka_images.sh <IMAGE_TAG> <AWS_ACCOUNT_ID> [AWS_REGION]

if [ $# -lt 2 ]; then
  echo "Usage: $0 <IMAGE_TAG> <AWS_ACCOUNT_ID> [AWS_REGION]"
  exit 1
fi

IMAGE_TAG="$1"
AWS_ACCOUNT_ID="$2"
AWS_REGION="${3:-ap-northeast-2}"
PROJECT_NAME="${PROJECT_NAME:-klp-logistics}"

ECR="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR}"

echo ">>> ECR login OK"

TARBALL="infra/kafka/kafka_2.13-3.7.0.tgz"
TARBALL_URL="https://archive.apache.org/dist/kafka/3.7.0/kafka_2.13-3.7.0.tgz"

fetch_tarball() {
  if [ -f "${TARBALL}" ]; then
    if tar -tzf "${TARBALL}" >/dev/null 2>&1; then
      echo "Kafka tarball already present: ${TARBALL}"
      return
    else
      echo "Existing tarball is corrupted. Re-downloading..."
      rm -f "${TARBALL}"
    fi
  fi

  echo "Kafka tarball not found locally, downloading..."
  # Retry; first attempt clean, second attempt resume
  if ! curl -fSL --retry 3 --retry-delay 2 "${TARBALL_URL}" -o "${TARBALL}"; then
    echo "Initial download failed, retrying with resume..."
    curl -fSL --retry 3 --retry-delay 2 --continue-at - "${TARBALL_URL}" -o "${TARBALL}" || true
  fi

  if [ ! -f "${TARBALL}" ]; then
    echo "Failed to download Kafka tarball. Place kafka_2.13-3.7.0.tgz under infra/kafka/ and rerun."
    exit 1
  fi

  if ! tar -tzf "${TARBALL}" >/dev/null 2>&1; then
    echo "Downloaded tarball is corrupted. Remove ${TARBALL} and rerun."
    exit 1
  fi
}

fetch_tarball

push_image() {
  local dir="$1"
  local name="$2"
  local dockerfile="$3"
  IMAGE_NAME="${ECR}/${PROJECT_NAME}-${name}"
  (
    cd "$(dirname "$0")/${dir}"
    docker build --platform linux/amd64 -f "${dockerfile}" -t "${IMAGE_NAME}:${IMAGE_TAG}" .
    docker push "${IMAGE_NAME}:${IMAGE_TAG}"
  )
}

push_image "." "kafka" "Dockerfile.kafka"
push_image "." "kafka-zookeeper" "Dockerfile.zookeeper"

# kafka-exporter는 공식 이미지 리태그
EXPORTER_SRC="danielqsj/kafka-exporter:latest"
EXPORTER_DST="${ECR}/${PROJECT_NAME}-kafka-exporter:${IMAGE_TAG}"
docker pull --platform linux/amd64 "${EXPORTER_SRC}"
docker tag "${EXPORTER_SRC}" "${EXPORTER_DST}"
docker push "${EXPORTER_DST}"

echo "All kafka images pushed."
