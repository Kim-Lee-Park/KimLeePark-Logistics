data "aws_caller_identity" "current" {}

resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${local.project}"
  retention_in_days = 7
}

resource "aws_service_discovery_private_dns_namespace" "ecs" {
  name        = "klp.local"
  description = "KLP ECS Services"
  vpc         = aws_vpc.main.id
}

locals {
  ecs_services = {
    config = {
      port          = 8888
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    discovery = {
      port          = 8761
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    gateway = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 2
      attach_to_alb = true
    }
    user = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    order = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 2
      attach_to_alb = false
    }
    hub = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    delivery = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    notification = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    promotion = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    payment = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
    auth = {
      port          = 8080
      cpu           = 1024
      memory        = 2048
      desired_count = 1
      attach_to_alb = false
    }
  }

  db_urls = {
    auth         = "jdbc:postgresql://${aws_db_instance.postgres["auth"].address}:5432/logistics?currentSchema=${local.db_targets.auth.schema}"
    delivery     = "jdbc:postgresql://${aws_db_instance.postgres["delivery"].address}:5432/logistics?currentSchema=${local.db_targets.delivery.schema}"
    hub          = "jdbc:postgresql://${aws_db_instance.postgres["hub"].address}:5432/logistics?currentSchema=${local.db_targets.hub.schema}"
    notification = "jdbc:postgresql://${aws_db_instance.postgres["notification"].address}:5432/logistics?currentSchema=${local.db_targets.notification.schema}"
    order        = "jdbc:postgresql://${aws_db_instance.postgres["order"].address}:5432/logistics?currentSchema=${local.db_targets.order.schema}"
    promotion    = "jdbc:postgresql://${aws_db_instance.postgres["promotion"].address}:5432/logistics?currentSchema=${local.db_targets.promotion.schema}"
    payment      = "jdbc:postgresql://${aws_db_instance.postgres["payment"].address}:5432/logistics?currentSchema=${local.db_targets.payment.schema}"
    user         = "jdbc:postgresql://${aws_db_instance.postgres["user"].address}:5432/logistics?currentSchema=${local.db_targets.user.schema}"
  }

  kafka_bootstrap = join(
    ",",
    [for i in aws_instance.kafka_broker : "${i.private_ip}:9092"]
  )

  otel_image = "${data.aws_ecr_repository.service["otel-collector"].repository_url}:latest"

  alb_server_url = "http://${aws_lb.public_alb.dns_name}"
}

resource "aws_service_discovery_service" "ecs" {
  for_each = local.ecs_services

  name = each.key

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "WEIGHTED"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}
