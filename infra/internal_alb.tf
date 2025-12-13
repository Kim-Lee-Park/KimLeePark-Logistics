resource "aws_lb" "internal_alb" {
  name               = "${local.project}-internal-alb"
  internal           = true
  load_balancer_type = "application"
  subnets = [aws_subnet.private_app_az1.id, aws_subnet.private_app_az2.id]
  security_groups = [aws_security_group.internal_alb.id]
}

locals {
  internal_services = {
    auth = { port = 8000 }
    user = { port = 8010 }
    order = { port = 8020 }
    hub = { port = 8030 }
    delivery = { port = 8040 }
    notification = { port = 8050 }
    promotion = { port = 9000 }
    payment = { port = 9020 }
  }
}

resource "aws_lb_target_group" "internal_blue" {
  for_each = local.internal_services

  name        = "${local.project_short}-int-${each.key}-b"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    healthy_threshold   = 3
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 15
    matcher             = "200-399"
  }
}

resource "aws_lb_target_group" "internal_green" {
  for_each = local.internal_services

  name        = "${local.project_short}-int-${each.key}-g"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    healthy_threshold   = 3
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 15
    matcher             = "200-399"
  }
}

resource "aws_lb_listener" "internal" {
  for_each = local.internal_services

  load_balancer_arn = aws_lb.internal_alb.arn
  port              = each.value.port
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.internal_blue[each.key].arn
  }
}
