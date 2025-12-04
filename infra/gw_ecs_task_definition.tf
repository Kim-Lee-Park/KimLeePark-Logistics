resource "aws_ecs_task_definition" "gateway" {
  family             = "${local.project}-gateway"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.gateway.cpu
  memory             = local.ecs_services.gateway.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "gateway"
      image     = "${data.aws_ecr_repository.service["gateway"].repository_url}:latest"
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
          "awslogs-stream-prefix" = "gateway"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "CONFIG_SERVER_URL", value = "config.klp.local" },
        { name = "EUREKA_URL", value = "discovery.klp.local" },
        { name = "GATEWAY_SERVICE_PORT", value = "8080" },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap },
        { name = "OTEL_EXPORTER_OTLP_ENDPOINT", value = "http://localhost:4317" },
        { name = "OTEL_SERVICE_NAME", value = "klp-logistics-gateway" }
      ]

      secrets = [
        {
          name      = "JWT_ACCESS_SECRET"
          valueFrom = data.aws_secretsmanager_secret.jwt_access_secret.arn
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
        }
      ]

      environment = [
        {
          name  = "AWS_REGION"
          value = var.aws_region
        },
        {
          name  = "OTEL_RESOURCE_ATTRIBUTES"
          value = "service.namespace=klp,service.name=klp-logistics-gateway"
        }
      ]

      command = ["--config=/etc/otel-config.yaml"]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "otel-gateway"
        }
      }
    }
  ])
}
