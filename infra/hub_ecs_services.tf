resource "aws_ecs_service" "hub" {
  name            = "${local.project}-hub-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.hub.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets = [aws_subnet.private_app_az1.id, aws_subnet.private_app_az2.id]
    security_groups = [aws_security_group.ecs_service.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.ecs["hub"].arn
  }

  depends_on = [
    aws_ecs_service.discovery
  ]
}
