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
      image     = "${aws_ecr_repository.service["promotion"].repository_url}:latest"
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
          "awslogs-stream-prefix" = "promotion"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "EUREKA_URL", value = "discovery.klp.local" },
        { name = "CONFIG_SERVER_URI", value = "config.klp.local" },
        { name = "PROMOTION_SERVICE_PORT", value = 8080 },
        { name = "PROMOTION_DB_DRIVER", value = "org.postgresql.Driver" },
        { name = "PROMOTION_DB_URL", value = local.db_urls.auth },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap }
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
    }
  ])
}
