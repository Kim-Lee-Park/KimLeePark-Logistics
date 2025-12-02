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
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
    discovery = {
      port          = 8761
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
    gateway = {
      port          = 8080
      cpu           = 512
      memory        = 1024
      desired_count = 2
      attach_to_alb = true
    }
    user = {
      port          = 8080
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
    order = {
      port          = 8080
      cpu           = 512
      memory        = 1024
      desired_count = 2
      attach_to_alb = false
    }
    hub = {
      port          = 8080
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
    delivery = {
      port          = 8080
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
    notification = {
      port          = 8080
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
    promotion = {
      port          = 8080
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
    auth = {
      port          = 8080
      cpu           = 256
      memory        = 1024
      desired_count = 1
      attach_to_alb = false
    }
  }

  db_urls = {
    auth         = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/logistics?currentSchema=auth_schema"
    delivery     = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/logistics?currentSchema=delivery_schema"
    hub          = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/logistics?currentSchema=hub_schema"
    notification = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/logistics?currentSchema=notification_schema"
    order        = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/logistics?currentSchema=order_schema"
    promotion    = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/logistics?currentSchema=promotion_schema"
    user         = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/logistics?currentSchema=user_schema"
  }

  kafka_bootstrap = join(
    ",",
    [for i in aws_instance.kafka_broker : "${i.private_ip}:9092"]
  )
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
