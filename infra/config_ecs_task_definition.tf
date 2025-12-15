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

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
        { name = "SPRING_CLOUD_CONFIG_ENABLED", value = "false" },
        { name = "SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL", value = "main" },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap },
        { name = "LOG_LEVEL", value = "ERROR" }
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
