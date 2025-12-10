locals {
  # Interface endpoints require one subnet per AZ; duplicates in the same AZ are rejected.
  vpc_endpoint_subnet_ids = [
    aws_subnet.private_app_az1.id, # AZ1
    aws_subnet.private_app_az2.id, # AZ2
  ]
}

# SG for Interface Endpoints (ECR API/DKR, STS)
resource "aws_security_group" "vpc_endpoints" {
  name        = "${local.project}-vpce-sg"
  description = "Allow VPC internal HTTPS to interface endpoints"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Allow VPC internal HTTPS to endpoints"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project}-vpce-sg"
  }
}

# ECR API endpoint (auth & manifest)
resource "aws_vpc_endpoint" "ecr_api" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.api"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = local.vpc_endpoint_subnet_ids
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name = "${local.project}-vpce-ecr-api"
  }
}

# ECR DKR endpoint (image layers)
resource "aws_vpc_endpoint" "ecr_dkr" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.dkr"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = local.vpc_endpoint_subnet_ids
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name = "${local.project}-vpce-ecr-dkr"
  }
}

# STS endpoint (needed for ECR auth token)
resource "aws_vpc_endpoint" "sts" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${var.aws_region}.sts"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = local.vpc_endpoint_subnet_ids
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name = "${local.project}-vpce-sts"
  }
}

# Secrets Manager endpoint (secret fetch without NAT)
resource "aws_vpc_endpoint" "secretsmanager" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${var.aws_region}.secretsmanager"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = local.vpc_endpoint_subnet_ids
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name = "${local.project}-vpce-secretsmanager"
  }
}

# KMS endpoint (envelope decryption without NAT)
resource "aws_vpc_endpoint" "kms" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${var.aws_region}.kms"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = local.vpc_endpoint_subnet_ids
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name = "${local.project}-vpce-kms"
  }
}

# CloudWatch Logs endpoint (ECS/EC2 to push logs without NAT)
resource "aws_vpc_endpoint" "logs" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${var.aws_region}.logs"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = local.vpc_endpoint_subnet_ids
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true

  tags = {
    Name = "${local.project}-vpce-logs"
  }
}

# S3 Gateway endpoint (ECR pulls depend on S3 for layers)
resource "aws_vpc_endpoint" "s3" {
  vpc_id            = aws_vpc.main.id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [aws_route_table.private_app.id]

  tags = {
    Name = "${local.project}-vpce-s3"
  }
}
