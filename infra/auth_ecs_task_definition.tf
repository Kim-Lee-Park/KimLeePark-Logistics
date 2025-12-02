resource "aws_ecs_task_definition" "auth" {
  family             = "${local.project}-auth"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.auth.cpu
  memory             = local.ecs_services.auth.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "auth"
      image     = "${aws_ecr_repository.service["auth"].repository_url}:latest"
      essential = true

      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "auth"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "EUREKA_URL", value = "discovery.klp.local" },
        { name = "CONFIG_SERVER_URI", value = "config.klp.local" },
        { name = "AUTH_SERVICE_PORT", value = 8080 },
        { name = "AUTH_DB_DRIVER", value = "org.postgresql.Driver" },
        { name = "AUTH_DB_URL", value = local.db_urls.auth },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap },
        { name = "JWT_ACCESS_EXPIRATION", value = "3600000" },
        { name = "JWT_REFRESH_EXPIRATION", value = "604800000" }
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
          valueForm = data.aws_secretsmanager_secret.jwt_refresh_secret.arn
        }
      ]
    }
  ])
}
