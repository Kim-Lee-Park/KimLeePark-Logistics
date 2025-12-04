resource "aws_ecs_task_definition" "discovery" {
  family             = "${local.project}-discovery"
  network_mode       = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                = local.ecs_services.discovery.cpu
  memory             = local.ecs_services.discovery.memory
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "discovery"
      image     = "${data.aws_ecr_repository.service["discovery"].repository_url}:latest"
      essential = true

      portMappings = [
        {
          containerPort = 8761
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "discovery"
        }
      }

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "EUREKA_HOSTNAME", value = "discovery.klp.local" },
        { name = "EUREKA_URL", value = "http://discovery.klp.local:8761/eureka/" },
        { name = "CONFIG_SERVER_URL", value = "http://config.klp.local:8888" },
        { name = "DISCOVERY_SERVICE_PORT", value = "8761" },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = local.kafka_bootstrap }
      ]
    }
  ])
}
