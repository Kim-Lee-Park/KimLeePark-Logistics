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
    "otel-collector",
    "loki",
    "tempo",
    "prometheus",
    "grafana"
  ]
}

resource "aws_ecr_repository" "service" {
  for_each = toset(local.ecr_services)

  name = "${local.project}-${each.key}"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "${local.project}-${each.key}-repo"
  }
}
