locals {
  internal_services_codedeploy = {
    auth         = { port = 8000 }
    user         = { port = 8010 }
    order        = { port = 8020 }
    hub          = { port = 8030 }
    delivery     = { port = 8040 }
    notification = { port = 8050 }
    promotion    = { port = 9000 }
    payment      = { port = 9020 }
  }

  internal_service_names = {
    auth         = aws_ecs_service.auth.name
    user         = aws_ecs_service.user.name
    order        = aws_ecs_service.order.name
    hub          = aws_ecs_service.hub.name
    delivery     = aws_ecs_service.delivery.name
    notification = aws_ecs_service.notification.name
    promotion    = aws_ecs_service.promotion.name
    payment      = aws_ecs_service.payment.name
  }
}

# CodeDeploy application & deployment groups for internal services
resource "aws_codedeploy_app" "internal" {
  for_each = local.internal_services_codedeploy

  name             = "${local.project}-${each.key}-app"
  compute_platform = "ECS"
}

resource "aws_codedeploy_deployment_group" "internal" {
  for_each = local.internal_services_codedeploy

  app_name               = aws_codedeploy_app.internal[each.key].name
  deployment_group_name  = "${local.project}-${each.key}-dg"
  service_role_arn       = aws_iam_role.codedeploy_ecs.arn
  deployment_config_name = "CodeDeployDefault.ECSAllAtOnce"

  deployment_style {
    deployment_option = "WITH_TRAFFIC_CONTROL"
    deployment_type   = "BLUE_GREEN"
  }

  blue_green_deployment_config {
    deployment_ready_option {
      action_on_timeout    = "CONTINUE_DEPLOYMENT"
      wait_time_in_minutes = 0
    }

    terminate_blue_instances_on_deployment_success {
      action                           = "TERMINATE"
      termination_wait_time_in_minutes = 5
    }
  }

  load_balancer_info {
    target_group_pair_info {
      prod_traffic_route {
        listener_arns = [aws_lb_listener.internal[each.key].arn]
      }

      target_group {
        name = aws_lb_target_group.internal_blue[each.key].name
      }

      target_group {
        name = aws_lb_target_group.internal_green[each.key].name
      }
    }
  }

  ecs_service {
    cluster_name = aws_ecs_cluster.main.name
    service_name = local.internal_service_names[each.key]
  }
}
