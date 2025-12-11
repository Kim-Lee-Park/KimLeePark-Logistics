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

resource "aws_security_group" "internal_alb" {
  name        = "${local.project}-internal-alb-sg"
  description = "Internal ALB Security Group"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 8000
    to_port   = 9020
    protocol  = "tcp"
    cidr_blocks = [aws_vpc.main.cidr_block]
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-internal-alb-sg"
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
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    security_groups = [aws_security_group.internal_alb.id]
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

  # Allow OTLP (Tempo) and Loki from ECS tasks
  ingress {
    from_port = 4317
    to_port   = 4317
    protocol  = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }

  ingress {
    from_port = 4318
    to_port   = 4318
    protocol  = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }

  ingress {
    from_port = 3100
    to_port   = 3100
    protocol  = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }

  ingress {
    from_port = 3200
    to_port   = 3200
    protocol  = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
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

resource "aws_security_group_rule" "kafka_exporter_from_obs" {
  type                     = "ingress"
  from_port                = 9308
  to_port                  = 9308
  protocol                 = "tcp"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.observability_stack.id
}

# Allow Prometheus in the observability stack to scrape ECS OTEL sidecars on 9464
resource "aws_security_group_rule" "ecs_otel_scrape_from_obs" {
  type                     = "ingress"
  from_port                = 9464
  to_port                  = 9464
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ecs_service.id
  source_security_group_id = aws_security_group.observability_stack.id
}
