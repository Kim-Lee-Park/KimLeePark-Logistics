resource "aws_lb" "public_alb" {
  name               = "${local.project}-alb"
  internal           = false
  load_balancer_type = "application"
  subnets = [aws_subnet.public_az1.id, aws_subnet.public_az2.id]
  security_groups = [aws_security_group.alb.id]
}

resource "aws_lb_target_group" "gateway_tg" {
  name     = "${local.project}-gateway-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

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

resource "aws_lb_target_group" "gateway_tg_green" {
  name     = "${local.project}-gateway-green-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

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

resource "aws_lb_target_group" "grafana_tg" {
  name     = "${local.project}-grafana-tg"
  port     = 3000
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  target_type = "ip"

  health_check {
    path                = "/api/health"
    healthy_threshold   = 3
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 15
    matcher             = "200-399"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.public_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.gateway_tg.arn
  }
}

resource "aws_lb_listener_rule" "grafana" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 10

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.grafana_tg.arn
  }

  condition {
    path_pattern {
      values = ["/grafana*", "/grafana/*"]
    }
  }
}

resource "aws_lb_target_group_attachment" "grafana_obs" {
  target_group_arn = aws_lb_target_group.grafana_tg.arn
  target_id        = aws_instance.observability_stack.private_ip
  port             = 3000
}
