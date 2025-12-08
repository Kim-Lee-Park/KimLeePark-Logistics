#!/usr/bin/env bash

set -eo pipefail

usage() {
  echo "Usage: $0 <IMAGE_TAG> <AWS_ACCOUNT_ID> [AWS_REGION]"
  exit 1
}

if [ $# -lt 2 ]; then
  usage
fi

IMAGE_TAG="$1"
AWS_ACCOUNT_ID="$2"
AWS_REGION="${3:-ap-northeast-2}"

echo ">>> IMAGE_TAG      = ${IMAGE_TAG}"
echo ">>> AWS_ACCOUNT_ID = ${AWS_ACCOUNT_ID}"
echo ">>> AWS_REGION     = ${AWS_REGION}"
echo

aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login \
      --username AWS \
      --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo ">>> ECR login OK"
echo

SERVICES_DIRS=(
  "auth-service"
  "config-server"
  "delivery"
  "discovery-service"
  "gateway-service"
  "hub"
  "notification"
  "order"
  "promotion"
  "user"
)

for SERVICE_DIR in "${SERVICES_DIRS[@]}"; do
  if [ ! -d "${SERVICE_DIR}" ]; then
    echo "** Skip: directory '${SERVICE_DIR}' not found **"
    continue
  fi

  case "${SERVICE_DIR}" in
    auth-service)      SERVICE="auth" ;;
    config-server)     SERVICE="config" ;;
    discovery-service) SERVICE="discovery" ;;
    gateway-service)   SERVICE="gateway" ;;
    *)                 SERVICE="${SERVICE_DIR}" ;;
  esac

  IMAGE_NAME="klp-logistics-${SERVICE}"
  ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${IMAGE_NAME}"

  echo "=================================================================="
  echo ">>> Building service jar with Gradle: ${SERVICE_DIR}"
  echo "=================================================================="

  (
    cd "${SERVICE_DIR}"
    ./gradlew clean bootJar -x test --no-daemon
  )

  echo "=================================================================="
  echo ">>> Docker build & push: ${SERVICE} -> ${ECR_URI}:${IMAGE_TAG}"
  echo "=================================================================="

  docker build \
    --platform linux/amd64 \
    -t "${ECR_URI}:${IMAGE_TAG}" \
    "${SERVICE_DIR}"

  docker push "${ECR_URI}:${IMAGE_TAG}"

  echo ">>> Done: ${SERVICE}"
  echo
done

echo "All jars built & images pushed."
