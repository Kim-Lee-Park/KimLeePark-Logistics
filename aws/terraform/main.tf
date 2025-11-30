terraform {
  required_version = ">= 1.7.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region
}

data "aws_availability_zones" "available" {
  state = "available"
}

# -------------------------------
# VPC & Subnets
# -------------------------------
resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.project_name}-igw"
  }
}

resource "aws_subnet" "public_az1" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidr_az1
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-az1"
  }
}

resource "aws_subnet" "public_az2" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnet_cidr_az2
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-az2"
  }
}

resource "aws_subnet" "private_app_az1" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.private_app_subnet_cidr_az1
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.project_name}-private-app-az1"
  }
}

resource "aws_subnet" "private_db_az1" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.private_db_subnet_cidr_az1
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.project_name}-private-db-az1"
  }
}

resource "aws_subnet" "private_db_az2" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.private_db_subnet_cidr_az2
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.project_name}-private-db-az2"
  }
}

# -------------------------------
# Route Tables (Public / Private with NAT Gateway)
# -------------------------------
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

resource "aws_route" "public_internet" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "public_az1" {
  subnet_id      = aws_subnet.public_az1.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_az2" {
  subnet_id      = aws_subnet.public_az2.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private_app" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.project_name}-private-app-rt"
  }
}

resource "aws_route_table" "private_db" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.project_name}-private-db-rt"
  }
}

resource "aws_route_table_association" "private_app_az1" {
  subnet_id      = aws_subnet.private_app_az1.id
  route_table_id = aws_route_table.private_app.id
}

resource "aws_route_table_association" "private_db_az1" {
  subnet_id      = aws_subnet.private_db_az1.id
  route_table_id = aws_route_table.private_db.id
}

resource "aws_route_table_association" "private_db_az2" {
  subnet_id      = aws_subnet.private_db_az2.id
  route_table_id = aws_route_table.private_db.id
}

# -------------------------------
# NAT Gateway (for Private Subnets)
# -------------------------------
resource "aws_eip" "nat" {
  vpc = true

  tags = {
    Name = "${var.project_name}-nat-eip"
  }
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_az1.id

  tags = {
    Name = "${var.project_name}-nat-gateway"
  }
}

resource "aws_route" "private_app_default" {
  route_table_id         = aws_route_table.private_app.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id         = aws_nat_gateway.this.id
}

resource "aws_route" "private_db_default" {
  route_table_id         = aws_route_table.private_db.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id         = aws_nat_gateway.this.id
}

# -------------------------------
# Security Groups
# -------------------------------
resource "aws_security_group" "alb" {
  name        = "${var.project_name}-alb-sg"
  description = "Allow HTTP from internet"
  vpc_id      = aws_vpc.this.id

  ingress {
    protocol    = "tcp"
    from_port   = 80
    to_port     = 80
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ecs" {
  name        = "${var.project_name}-ecs-sg"
  description = "Allow HTTP from ALB"
  vpc_id      = aws_vpc.this.id

  ingress {
    protocol        = "tcp"
    from_port       = 8080
    to_port         = 8080
    security_groups = [aws_security_group.alb.id]
    description     = "From ALB"
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "rds" {
  name        = "${var.project_name}-rds-sg"
  description = "Allow PostgreSQL from ECS"
  vpc_id      = aws_vpc.this.id

  ingress {
    protocol        = "tcp"
    from_port       = 5432
    to_port         = 5432
    security_groups = [aws_security_group.ecs.id]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# -------------------------------
# ALB (HTTP only)
# -------------------------------
resource "aws_lb" "this" {
  name               = "${var.project_name}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = [aws_subnet.public_az1.id, aws_subnet.public_az2.id]
}

# Target Groups (Deregistration delay: 30초로 무중단 배포에 유리)
resource "aws_lb_target_group" "order" {
  name        = "${var.project_name}-order-tg"
  vpc_id      = aws_vpc.this.id
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"

  deregistration_delay = 30

  health_check {
    path = "/actuator/health"
  }
}

resource "aws_lb_target_group" "payment" {
  name        = "${var.project_name}-payment-tg"
  vpc_id      = aws_vpc.this.id
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"

  deregistration_delay = 30

  health_check {
    path = "/actuator/health"
  }
}

resource "aws_lb_target_group" "product" {
  name        = "${var.project_name}-product-tg"
  vpc_id      = aws_vpc.this.id
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"

  deregistration_delay = 30

  health_check {
    path = "/actuator/health"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.this.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.order.arn
  }
}

# /payments*, /payment* → payment TG
resource "aws_lb_listener_rule" "payment" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 10

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.payment.arn
  }

  condition {
    path_pattern {
      values = ["/payments*", "/payment*"]
    }
  }
}

# /products*, /product* → product TG
resource "aws_lb_listener_rule" "product" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 20

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.product.arn
  }

  condition {
    path_pattern {
      values = ["/products*", "/product*"]
    }
  }
}

# -------------------------------
# CloudWatch Log Groups
# -------------------------------
resource "aws_cloudwatch_log_group" "order" {
  name              = "/ecs/${var.project_name}/order"
  retention_in_days = 7
}

resource "aws_cloudwatch_log_group" "payment" {
  name              = "/ecs/${var.project_name}/payment"
  retention_in_days = 7
}

resource "aws_cloudwatch_log_group" "product" {
  name              = "/ecs/${var.project_name}/product"
  retention_in_days = 7
}

# -------------------------------
# ECS Cluster & Task Execution Role
# -------------------------------
resource "aws_ecs_cluster" "this" {
  name = "${var.project_name}-cluster"
}

resource "aws_iam_role" "ecs_task_execution" {
  name = "${var.project_name}-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Name = "${var.project_name}-ecs-execution-role"
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# -------------------------------
# ECS Task Definitions
# -------------------------------
locals {
  common_container_env = [
    {
      name  = "DB_HOST"
      value = aws_db_instance.rds.address
    },
    {
      name  = "DB_PORT"
      value = "5432"
    },
    {
      name  = "DB_USERNAME"
      value = var.rds_master_username
    },
    {
      name  = "DB_PASSWORD"
      value = var.rds_master_password
    }
  ]
}

resource "aws_ecs_task_definition" "order" {
  family                   = "${var.project_name}-order-task"
  cpu                      = "512"
  memory                   = "1024"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn

  runtime_platform {
    cpu_architecture        = "ARM64"
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([
    {
      name  = "order-service"
      image = var.order_image
      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.order.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
      environment = local.common_container_env
    }
  ])
}

resource "aws_ecs_task_definition" "payment" {
  family                   = "${var.project_name}-payment-task"
  cpu                      = "256"
  memory                   = "1024"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn

  runtime_platform {
    cpu_architecture        = "ARM64"
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([
    {
      name  = "payment-service"
      image = var.payment_image
      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.payment.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
      environment = local.common_container_env
    }
  ])
}

resource "aws_ecs_task_definition" "product" {
  family                   = "${var.project_name}-product-task"
  cpu                      = "256"
  memory                   = "1024"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn

  runtime_platform {
    cpu_architecture        = "ARM64"
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([
    {
      name  = "product-service"
      image = var.product_image
      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.product.name
          awslogs-region        = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
      environment = local.common_container_env
    }
  ])
}

# -------------------------------
# ECS Services (Rolling Update 기본 사용)
# -------------------------------
resource "aws_ecs_service" "order" {
  name            = "${var.project_name}-order-service"
  cluster         = aws_ecs_cluster.this.id
  desired_count   = var.order_desired_count
  launch_type     = "FARGATE"
  task_definition = aws_ecs_task_definition.order.arn

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  health_check_grace_period_seconds = 30

  network_configuration {
    assign_public_ip = false
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.private_app_az1.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.order.arn
    container_name   = "order-service"
    container_port   = 8080
  }

  depends_on = [
    aws_lb_listener.http,
    aws_cloudwatch_log_group.order,
    aws_db_instance.rds
  ]
}

resource "aws_ecs_service" "payment" {
  name            = "${var.project_name}-payment-service"
  cluster         = aws_ecs_cluster.this.id
  desired_count   = var.payment_desired_count
  launch_type     = "FARGATE"
  task_definition = aws_ecs_task_definition.payment.arn

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  health_check_grace_period_seconds = 30

  network_configuration {
    assign_public_ip = false
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.private_app_az1.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.payment.arn
    container_name   = "payment-service"
    container_port   = 8080
  }

  depends_on = [
    aws_lb_listener.http,
    aws_cloudwatch_log_group.payment,
    aws_db_instance.rds
  ]
}

resource "aws_ecs_service" "product" {
  name            = "${var.project_name}-product-service"
  cluster         = aws_ecs_cluster.this.id
  desired_count   = var.product_desired_count
  launch_type     = "FARGATE"
  task_definition = aws_ecs_task_definition.product.arn

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  health_check_grace_period_seconds = 30

  network_configuration {
    assign_public_ip = false
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.private_app_az1.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.product.arn
    container_name   = "product-service"
    container_port   = 8080
  }

  depends_on = [
    aws_lb_listener.http,
    aws_cloudwatch_log_group.product,
    aws_db_instance.rds
  ]
}

# -------------------------------
# RDS (PostgreSQL, Multi-AZ)
# -------------------------------
resource "aws_db_subnet_group" "this" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = [aws_subnet.private_db_az1.id, aws_subnet.private_db_az2.id]

  tags = {
    Name = "${var.project_name} db subnet group"
  }
}

resource "aws_db_instance" "rds" {
  engine               = "postgres"
  engine_version       = "15"
  instance_class       = "db.t4g.micro"
  allocated_storage    = var.rds_allocated_storage
  username             = var.rds_master_username
  password             = var.rds_master_password
  db_subnet_group_name = aws_db_subnet_group.this.name
  vpc_security_group_ids = [
    aws_security_group.rds.id
  ]

  multi_az                = true
  publicly_accessible     = false
  deletion_protection     = false
  backup_retention_period = 7
  storage_type            = "gp3"
  db_name                 = "logistics"

  skip_final_snapshot = true
}

# -------------------------------
# WAF + Association (ALB)
# -------------------------------
resource "aws_wafv2_web_acl" "this" {
  name  = "${var.project_name}-waf"
  scope = "REGIONAL"

  default_action {
    allow {}
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.project_name}-waf-metric"
    sampled_requests_enabled   = true
  }

  rule {
    name     = "AwsCommonRules"
    priority = 1

    override_action {
      none {}
    }

    statement {
      managed_rule_group_statement {
        vendor_name = "AWS"
        name        = "AWSManagedRulesCommonRuleSet"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${var.project_name}-waf-common"
      sampled_requests_enabled   = true
    }
  }
}

resource "aws_wafv2_web_acl_association" "alb" {
  resource_arn = aws_lb.this.arn
  web_acl_arn  = aws_wafv2_web_acl.this.arn
}
