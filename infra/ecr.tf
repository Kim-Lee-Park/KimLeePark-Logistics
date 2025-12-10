data "aws_ecr_repository" "service" {
  for_each = toset(local.ecr_services)

  name = "${local.project}-${each.key}"
}
locals {
  ecr_services = [
    "discovery",
    "config",
    "gateway",
    "order",
    "auth",
    "user",
    "hub",
    "delivery",
    "notification",
    "promotion",
    "payment",
    "otel-collector",
    "loki",
    "tempo",
    "prometheus",
    "grafana",
    "kafka",
    "kafka-exporter",
    "kafka-zookeeper"
  ]
}
