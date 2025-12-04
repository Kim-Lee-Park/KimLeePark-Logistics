resource "aws_ecs_service" "promotion" {
  name            = "${local.project}-promotion-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.promotion.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets = [aws_subnet.private_app_az1.id, aws_subnet.private_app_az2.id]
    security_groups = [aws_security_group.ecs_service.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.ecs["promotion"].arn
  }

  depends_on = [
    aws_ecs_service.discovery
  ]
}
