locals {
  envs = ["prod", "stage"]

  ecr_services_env = [
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

  ecr_targets_env = flatten([
    for env in local.envs : [
      for service in local.ecr_services_env : {
        env     = env
        service = service
        shared  = false
      }
    ]
  ])

  ecr_targets_shared = [
    for service in local.ecr_services_shared : {
      env     = null
      service = service
      shared  = true
    }
  ]

  ecr_targets = concat(local.ecr_targets_env, local.ecr_targets_shared)
}

resource "aws_ecr_repository" "service" {
  for_each = {
    for target in local.ecr_targets :
    "${target.env != null ? target.env : "shared"}-${target.service}" => target
  }

  name = each.value.shared ? "${var.project_name}-${each.value.service}" : "${var.project_name}-${each.value.env}-${each.value.service}"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name        = each.value.shared ? "${var.project_name}-${each.value.service}-repo" : "${var.project_name}-${each.value.env}-${each.value.service}-repo"
    Environment = each.value.shared ? "shared" : each.value.env
  }
}
