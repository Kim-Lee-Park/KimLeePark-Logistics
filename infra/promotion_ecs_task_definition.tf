resource "aws_ecs_task_definition" "promotion" {
  family             = "${local.project}-promotion"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.promotion.cpu
  memory             = local.ecs_services.promotion.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "promotion"
      image     = "${data.aws_ecr_repository.service["promotion"].repository_url}:latest"
      essential = true

      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]

      dependsOn = [
        {
          containerName = "otel-collector"
          condition     = "START"
        }
      ]

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE",
          value = var.environment
        },
        {
          name  = "CONFIG_SERVER_URL",
          value = "http://config.klp.local:8888"
        },
        {
          name  = "PROMOTION_DOMAIN_NAME",
          value = "promotion.klp.local"
        },
        {
          name  = "PROMOTION_SERVICE_PORT",
          value = "8080"
        },
        {
          name  = "PROMOTION_DB_DRIVER",
          value = "org.postgresql.Driver"
        },
        {
          name  = "PROMOTION_DB_URL",
          value = local.db_urls.promotion
        },
        {
          name  = "INTERNAL_ALB_HOST",
          value = aws_lb.internal_alb.dns_name
        },
        {
          name  = "REDIS_HOST",
          value = aws_elasticache_cluster.redis.cache_nodes[0].address
        },
        {
          name = "REDIS_PORT",
          value = tostring(aws_elasticache_cluster.redis.port)
        },
        {
          name  = "SERVER_URL",
          value = local.alb_server_url
        },
        {
          name  = "KAFKA_BOOTSTRAP_SERVERS",
          value = local.kafka_bootstrap
        },
        {
          name  = "OTEL_EXPORTER_OTLP_ENDPOINT",
          value = "http://localhost:4318"
        },
        {
          name  = "OTEL_EXPORTER_OTLP_TRACES_ENDPOINT",
          value = "http://localhost:4318/v1/traces"
        },
        {
          name  = "OTEL_EXPORTER_OTLP_LOGS_ENDPOINT",
          value = "http://localhost:4318/v1/logs"
        },
        {
          name  = "OTEL_EXPORTER_OTLP_METRICS_ENDPOINT",
          value = "http://localhost:4318/v1/metrics"
        },
        {
          name  = "OTEL_TRACES_EXPORTER",
          value = "otlp"
        },
        {
          name  = "OTEL_METRICS_EXPORTER",
          value = "otlp"
        },
        {
          name  = "OTEL_LOGS_EXPORTER",
          value = "otlp"
        },
        {
          name  = "OTEL_SERVICE_NAME",
          value = "klp-logistics-promotion"
        },
        {
          name  = "OTEL_RESOURCE_ATTRIBUTES",
          value = "service.namespace=klp"
        },
        {
          name  = "OTEL_INSTRUMENTATION_HTTP_SERVER_EXCLUDE_PATTERNS",
          value = "/actuator/.*,/swagger-ui/.*,/v3/api-docs/.*,/v1/api-docs/.*"
        }
      ]

      secrets = [
        {
          name      = "PROMOTION_DB_USERNAME"
          valueFrom = data.aws_secretsmanager_secret.db_username.arn
        },
        {
          name      = "PROMOTION_DB_PASSWORD"
          valueFrom = data.aws_secretsmanager_secret.db_password.arn
        }
      ]
    },
    {
      name      = "otel-collector"
      image     = local.otel_image
      essential = false

      portMappings = [
        {
          containerPort = 4317
          protocol      = "tcp"
        },
        {
          containerPort = 4318
          protocol      = "tcp"
        },
        {
          containerPort = 9464,
          protocol      = "tcp"
        }
      ]

      environment = [
        {
          name  = "AWS_REGION"
          value = var.aws_region
        },
        {
          name  = "OTEL_LOG_LEVEL"
          value = "error"
        },
        {
          name  = "TEMPO_HOST"
          value = aws_instance.observability_stack.private_ip
        },
        {
          name  = "LOKI_HOST"
          value = aws_instance.observability_stack.private_ip
        },
        {
          name  = "OTEL_RESOURCE_ATTRIBUTES"
          value = "service.namespace=klp,service.name=otel-collector"
        }
        ,
        {
          name  = "OTEL_INSTRUMENTATION_HTTP_SERVER_EXCLUDE_PATTERNS"
          value = "/actuator/.*,/swagger-ui/.*,/v3/api-docs/.*,/v1/api-docs/.*"
        },
        {
          name  = "OTEL_EXPORTER_OTLP_COMPRESSION"
          value = "gzip"
        }
      ]
    }
  ])
}
