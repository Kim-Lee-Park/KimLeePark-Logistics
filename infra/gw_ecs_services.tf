resource "aws_ecs_service" "gateway" {
  name            = "${local.project}-gateway-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.gateway.arn
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
    target_group_arn = aws_lb_target_group.gateway_tg.arn
    container_name   = "gateway"
    container_port   = 8080
  }

  depends_on = [
    aws_ecs_service.config,
    aws_lb_target_group.gateway_tg,
    aws_lb_target_group.gateway_tg_green
  ]
}
