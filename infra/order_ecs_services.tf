resource "aws_ecs_service" "order" {
  name            = "${local.project}-order-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.order.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  deployment_controller {
    type = "CODE_DEPLOY"
  }

  network_configuration {
    subnets = [aws_subnet.private_app_az1.id, aws_subnet.private_app_az2.id]
    security_groups = [aws_security_group.ecs_service.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.internal_blue["order"].arn
    container_name   = "order"
    container_port   = 8080
  }

  service_registries {
    registry_arn = aws_service_discovery_service.ecs["order"].arn
  }

  depends_on = [
    aws_lb_target_group.internal_blue["order"],
    aws_lb_target_group.internal_green["order"]
  ]
}
