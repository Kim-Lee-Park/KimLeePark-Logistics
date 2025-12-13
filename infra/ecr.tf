locals {
  ecr_services_env = [
    "config",
    "gateway",
    "order",
    "auth",
    "user",
    "hub",
    "delivery",
    "notification",
    "promotion",
    "payment"
  ]
  ecr_services_shared = [
    "otel-collector",
    "loki",
    "tempo",
    "prometheus",
    "grafana",
    "kafka",
    "kafka-exporter",
    "kafka-zookeeper"
  ]
  ecr_services = concat(local.ecr_services_env, local.ecr_services_shared)
}

data "aws_ecr_repository" "service" {
  for_each = toset(local.ecr_services)

  name = contains(local.ecr_services_shared, each.key) ? "${var.project_name}-${each.key}" : "${var.project_name}-${var.environment}-${each.key}"
}
