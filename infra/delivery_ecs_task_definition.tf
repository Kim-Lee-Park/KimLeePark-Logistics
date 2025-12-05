resource "aws_ecs_task_definition" "delivery" {
  family             = "${local.project}-delivery"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.delivery.cpu
  memory             = local.ecs_services.delivery.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "delivery"
      image     = "${data.aws_ecr_repository.service["delivery"].repository_url}:latest"
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
          "awslogs-stream-prefix" = "delivery"
        }
      }

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE",
          value = "prod"
        },
        {
          name  = "EUREKA_URL",
          value = "http://discovery.klp.local:8761/eureka/"
        },
        {
          name  = "CONFIG_SERVER_URL",
          value = "http://config.klp.local:8888"
        },
        {
          name  = "DELIVERY_SERVICE_PORT",
          value = "8080"
        },
        {
          name  = "DELIVERY_DB_DRIVER",
          value = "org.postgresql.Driver"
        },
        {
          name  = "DELIVERY_DB_URL",
          value = local.db_urls.delivery
        },
        {
          name  = "SCHEDULER_ROUTE_PLAN_DELETE_FIXED_RATE",
          value = "PT3H"
        },
        {
          name  = "SCHEDULER_ROUTE_PLAN_DELETE_INITIAL_DELAY",
          value = "PT0S"
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
          value = "klp-logistics-delivery"
        },
        {
          name  = "OTEL_RESOURCE_ATTRIBUTES",
          value = "service.namespace=klp"
        }
      ]

      secrets = [
        {
          name      = "DELIVERY_DB_USERNAME"
          valueFrom = data.aws_secretsmanager_secret.db_username.arn
        },
        {
          name      = "DELIVERY_DB_PASSWORD"
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
          name  = "TEMPO_HOST"
          value = aws_instance.observability_stack.private_ip
        },
        {
          name  = "LOKI_HOST"
          value = aws_instance.observability_stack.private_ip
        },
        {
          name  = "OTEL_RESOURCE_ATTRIBUTES"
          value = "service.namespace=klp,service.name=klp-logistics-delivery"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "otel-delivery"
        }
      }
    }
  ])
}
