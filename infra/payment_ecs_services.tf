resource "aws_ecs_service" "payment" {
  name            = "${local.project}-payment-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.payment.arn
  desired_count   = local.ecs_services.payment.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = [aws_subnet.private_app_az1.id, aws_subnet.private_app_az2.id]
    security_groups = [aws_security_group.ecs_service.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.ecs["payment"].arn
  }

  depends_on = [
    aws_ecs_service.discovery
  ]
}
