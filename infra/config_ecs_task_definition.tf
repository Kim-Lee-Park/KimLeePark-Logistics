resource "aws_ecs_task_definition" "config" {
  family             = "${local.project}-config"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.config.cpu
  memory             = local.ecs_services.config.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "config"
      image     = "${data.aws_ecr_repository.service["config"].repository_url}:latest"
      essential = true

      portMappings = [
        {
          containerPort = 8888
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "config"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "EUREKA_HOSTNAME", value = "discovery.klp.local" },
        { name = "CONFIG_SERVER_URL", value = "config.klp.local" },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap },
      ]

      secrets = [
        {
          name      = "CONFIG_REPO_URI"
          valueFrom = data.aws_secretsmanager_secret.config_repo_uri.arn
        },
        {
          name      = "GIT_USERNAME"
          valueFrom = data.aws_secretsmanager_secret.git_username.arn
        },
        {
          name      = "GIT_TOKEN"
          valueFrom = data.aws_secretsmanager_secret.git_token.arn
        }
      ]
    }
  ])
}
