resource "aws_security_group" "alb" {
  name        = "${local.project}-alb-sg"
  description = "ALB Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-alb-sg"
  }
}

resource "aws_security_group" "ecs_service" {
  name        = "${local.project}-ecs-sg"
  description = "ECS Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    from_port = 0
    to_port   = 65535
    protocol  = "tcp"
    self      = true
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-ecs-sg"
  }
}

resource "aws_security_group" "db" {
  name        = "${local.project}-db-sg"
  description = "RDS Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 5432
    to_port   = 5432
    protocol  = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }

  ingress {
    from_port = 5432
    to_port   = 5432
    protocol  = "tcp"
    security_groups = [aws_security_group.bastion.id]
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-db-sg"
  }
}

resource "aws_security_group" "redis" {
  name        = "${local.project}-redis-sg"
  description = "Redis Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 6379
    to_port   = 6379
    protocol  = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-redis-sg"
  }
}

resource "aws_security_group" "bastion" {
  name        = "${local.project}-bastion-sg"
  description = "Bastion Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-bastion-sg"
  }
}

resource "aws_security_group" "kafka" {
  name        = "${local.project}-kafka-sg"
  description = "Kafka Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 9092
    to_port   = 9092
    protocol  = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }

  ingress {
    from_port = 9092
    to_port   = 9092
    protocol  = "tcp"
    self      = true
  }

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    security_groups = [aws_security_group.bastion.id]
  }

  ingress {
    from_port = 2181
    to_port   = 2181
    protocol  = "tcp"
    self      = true
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-kafka-sg"
  }
}

resource "aws_security_group" "observability_stack" {
  name        = "${local.project}-obs-sg"
  description = "Observability Stack Security Group"
  vpc_id      = aws_vpc.main.id

  # Grafana
  ingress {
    from_port = 3000
    to_port   = 3000
    protocol  = "tcp"
    security_groups = [aws_security_group.bastion.id]
  }

  # Grafana via ALB
  ingress {
    from_port = 3000
    to_port   = 3000
    protocol  = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  # ECS, Kafka (Prometheus, Loki, Tempo, Otel Collector)
  ingress {
    from_port = 0
    to_port   = 65535
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_service.id,
      aws_security_group.kafka.id,
    ]
  }

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    security_groups = [aws_security_group.bastion.id]
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-obs-sg"
  }
}

resource "aws_security_group_rule" "obs_sg_kafka_otel" {
  type                     = "ingress"
  from_port                = 9464
  to_port                  = 9464
  protocol                 = "tcp"
  security_group_id        = aws_security_group.observability_stack.id
  source_security_group_id = aws_security_group.observability_stack.id
}
