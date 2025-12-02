resource "aws_ecs_task_definition" "hub" {
  family             = "${local.project}-hub"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.hub.cpu
  memory             = local.ecs_services.hub.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "hub"
      image     = "${aws_ecr_repository.service["hub"].repository_url}:latest"
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
          "awslogs-stream-prefix" = "hub"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "EUREKA_URL", value = "discovery.klp.local" },
        { name = "CONFIG_SERVER_URI", value = "config.klp.local" },
        { name = "HUB_SERVICE_PORT", value = 8080 },
        { name = "HUB_DB_DRIVER", value = "org.postgresql.Driver" },
        { name = "HUB_DB_URL", value = local.db_urls.auth },
        { name = "SCHEDULER_ROUTE_INFOS_FIXED_RATE", value = "PT2H" },
        { name = "SCHEDULER_ROUTE_INFOS_INITIAL_DELAY", value = "PT0S" },
        { name = "SCHEDULER_HUB_DELETE_FIXED_RATE", value = "PT3H" },
        { name = "SCHEDULER_HUB_DELETE_INITIAL_DELAY", value = "PT0S" },
        { name = "SHEDLOCK_ROUTE_INFOS_AT_LEAST", value = "1m" },
        { name = "SHEDLOCK_ROUTE_INFOS_AT_MOST", value = "30m" },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap }
      ]

      secrets = [
        {
          name      = "HUB_DB_USERNAME"
          valueFrom = data.aws_secretsmanager_secret.db_username.arn
        },
        {
          name      = "HUB_DB_PASSWORD"
          valueFrom = data.aws_secretsmanager_secret.db_password.arn
        }
      ]
    }
  ])
}
