resource "aws_ecs_task_definition" "auth" {
  family                   = "${local.project}-auth"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = local.ecs_services.auth.cpu
  memory                   = local.ecs_services.auth.memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "auth"
      image     = "${data.aws_ecr_repository.service["auth"].repository_url}:latest"
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
          name  = "AUTH_DOMAIN_NAME",
          value = "auth.klp.local"
        },
        {
          name  = "AUTH_SERVICE_PORT",
          value = "8080"
        },
        {
          name  = "AUTH_DB_DRIVER",
          value = "org.postgresql.Driver"
        },
        {
          name  = "AUTH_DB_URL",
          value = local.db_urls.auth
        },
        {
          name  = "INTERNAL_ALB_HOST",
          value = aws_lb.internal_alb.dns_name
        },
        {
          name  = "USER_SERVICE_PORT",
          value = "8010"
        },
        {
          name  = "KAFKA_BOOTSTRAP_SERVERS",
          value = local.kafka_bootstrap
        },
        {
          name  = "SERVER_URL",
          value = local.alb_server_url
        },
        {
          name  = "JWT_ACCESS_EXPIRATION",
          value = "3600000"
        },
        {
          name  = "JWT_REFRESH_EXPIRATION",
          value = "604800000"
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
          value = "klp-logistics-auth"
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
          name      = "AUTH_DB_USERNAME"
          valueFrom = data.aws_secretsmanager_secret.db_username.arn
        },
        {
          name      = "AUTH_DB_PASSWORD"
          valueFrom = data.aws_secretsmanager_secret.db_password.arn
        },
        {
          name      = "JWT_ACCESS_SECRET"
          valueFrom = data.aws_secretsmanager_secret.jwt_access_secret.arn
        },
        {
          name      = "JWT_REFRESH_SECRET"
          valueFrom = data.aws_secretsmanager_secret.jwt_refresh_secret.arn
        }
      ],
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
          value = "tempo.klp.local"
        },
        {
          name  = "LOKI_HOST"
          value = "loki.klp.local"
        },
        {
          name  = "OTEL_RESOURCE_ATTRIBUTES"
          value = "service.namespace=klp,service.name=otel-collector"
        }
      ]
    }
  ])
}
