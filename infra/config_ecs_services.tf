resource "aws_ecs_service" "config" {
  name            = "${local.project}-config-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.config.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets = [aws_subnet.private_app_az1.id, aws_subnet.private_app_az2.id]
    security_groups = [aws_security_group.ecs_service.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.ecs["config"].arn
  }

  depends_on = [
    null_resource.init_schemas["auth"],
    null_resource.init_schemas["delivery"],
    null_resource.init_schemas["hub"],
    null_resource.init_schemas["notification"],
    null_resource.init_schemas["order"],
    null_resource.init_schemas["promotion"],
    null_resource.init_schemas["payment"],
    null_resource.init_schemas["user"],
    aws_instance.kafka_broker
  ]
}
