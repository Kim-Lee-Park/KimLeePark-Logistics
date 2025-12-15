resource "aws_ecs_task_definition" "notification" {
  family             = "${local.project}-notification"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.notification.cpu
  memory             = local.ecs_services.notification.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "notification"
      image     = "${data.aws_ecr_repository.service["notification"].repository_url}:latest"
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

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "notification"
        }
      }

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
          name  = "NOTIFICATION_DOMAIN_NAME",
          value = "notification.klp.local"
        },
        {
          name  = "NOTIFICATION_SERVICE_PORT",
          value = "8080"
        },
        {
          name  = "NOTIFICATION_DB_DRIVER",
          value = "org.postgresql.Driver"
        },
        {
          name  = "NOTIFICATION_DB_URL",
          value = local.db_urls.notification
        },
        {
          name  = "INTERNAL_ALB_HOST",
          value = aws_lb.internal_alb.dns_name
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
          value = "klp-logistics-notification"
        },
        {
          name  = "OTEL_RESOURCE_ATTRIBUTES",
          value = "service.namespace=klp"
        }
      ]

      secrets = [
        {
          name      = "NOTIFICATION_DB_USERNAME"
          valueFrom = data.aws_secretsmanager_secret.db_username.arn
        },
        {
          name      = "NOTIFICATION_DB_PASSWORD"
          valueFrom = data.aws_secretsmanager_secret.db_password.arn
        },
        {
          name      = "GEMINI_API_KEY"
          valueFrom = data.aws_secretsmanager_secret.gemini_api_key.arn
        },
        {
          name      = "SLACK_BOT_TOKEN"
          valueFrom = data.aws_secretsmanager_secret.slack_bot_token.arn
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
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "otel-notification"
        }
      }
    }
  ])
}
