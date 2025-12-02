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
      image     = "${aws_ecr_repository.service["delivery"].repository_url}:latest"
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
          "awslogs-stream-prefix" = "delivery"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "EUREKA_URL", value = "discovery.klp.local" },
        { name = "CONFIG_SERVER_URI", value = "config.klp.local" },
        { name = "DELIVERY_SERVICE_PORT", value = 8080 },
        { name = "DELIVERY_DB_DRIVER", value = "org.postgresql.Driver" },
        { name = "DELIVERY_DB_URL", value = local.db_urls.auth },
        { name = "SCHEDULER_ROUTE_PLAN_DELETE_FIXED_RATE", value = "PT3H" },
        { name = "SCHEDULER_ROUTE_PLAN_DELETE_INITIAL_DELAY", value = "PT0S" },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap }
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
    }
  ])
}
