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
      image     = "${aws_ecr_repository.service["notification"].repository_url}:latest"
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
          "awslogs-stream-prefix" = "notification"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "EUREKA_URL", value = "discovery.klp.local" },
        { name = "CONFIG_SERVER_URI", value = "config.klp.local" },
        { name = "NOTIFICATION_SERVICE_PORT", value = 8080 },
        { name = "NOTIFICATION_DB_DRIVER", value = "org.postgresql.Driver" },
        { name = "NOTIFICATION_DB_URL", value = local.db_urls.auth },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap }
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
    }
  ])
}
