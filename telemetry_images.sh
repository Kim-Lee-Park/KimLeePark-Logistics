#!/usr/bin/env bash
set -eo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 <IMAGE_TAG> <AWS_ACCOUNT_ID> [AWS_REGION]"
  exit 1
fi

IMAGE_TAG="$1"
AWS_ACCOUNT_ID="$2"
AWS_REGION="${3:-ap-northeast-2}"

ECR="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR}"

echo ">>> ECR login OK"

# 1) otel-collector
(
  cd infra/telemetry-prod/otel-collector
  IMAGE_NAME="${ECR}/${PROJECT_NAME:-klp-logistics}-otel-collector"
  docker build \
      --platform linux/amd64 \
      -t "${IMAGE_NAME}:${IMAGE_TAG}" .
  docker push "${IMAGE_NAME}:${IMAGE_TAG}"
)

# 2) loki
(
  cd infra/telemetry-prod/loki
  IMAGE_NAME="${ECR}/${PROJECT_NAME:-klp-logistics}-loki"
  docker build \
      --platform linux/amd64 \
      -t "${IMAGE_NAME}:${IMAGE_TAG}" .
  docker push "${IMAGE_NAME}:${IMAGE_TAG}"
)

# 3) tempo
(
  cd infra/telemetry-prod/tempo
  IMAGE_NAME="${ECR}/${PROJECT_NAME:-klp-logistics}-tempo"
  docker build \
      --platform linux/amd64 \
      -t "${IMAGE_NAME}:${IMAGE_TAG}" .
  docker push "${IMAGE_NAME}:${IMAGE_TAG}"
)

# 4) prometheus
(
  cd infra/telemetry-prod/prometheus
  IMAGE_NAME="${ECR}/${PROJECT_NAME:-klp-logistics}-prometheus"
  docker build \
      --platform linux/amd64 \
      -t "${IMAGE_NAME}:${IMAGE_TAG}" .
  docker push "${IMAGE_NAME}:${IMAGE_TAG}"
)

# 5) grafana
(
  cd infra/telemetry-prod/grafana
  IMAGE_NAME="${ECR}/${PROJECT_NAME:-klp-logistics}-grafana"
  docker build \
      --platform linux/amd64 \
      -t "${IMAGE_NAME}:${IMAGE_TAG}" .
  docker push "${IMAGE_NAME}:${IMAGE_TAG}"
)

echo "All telemetry images pushed."
