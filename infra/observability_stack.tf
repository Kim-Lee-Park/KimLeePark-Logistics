resource "aws_iam_role" "obs_instance" {
  name = "${local.project}-obs-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "obs_ecr_readonly" {
  role       = aws_iam_role.obs_instance.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_instance_profile" "obs" {
  name = "${local.project}-obs-instance-profile"
  role = aws_iam_role.obs_instance.name
}

resource "aws_instance" "observability_stack" {
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name
  subnet_id     = aws_subnet.private_obs_az1.id
  vpc_security_group_ids = [aws_security_group.observability_stack.id]

  iam_instance_profile = aws_iam_instance_profile.obs.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 100
  }

  tags = {
    Name = "${local.project}-obs"
  }

  user_data = <<-EOF
    #!/bin/bash
    set -xe

    # 1) Docker 설치
    yum update -y
    yum install -y docker

    systemctl enable docker
    systemctl start docker

    # 2) docker-compose (v2) 설치
    curl -L "https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose

    mkdir -p /opt/telemetry
    cd /opt/telemetry

    # 3) ECR 로그인
    aws ecr get-login-password --region ${var.aws_region} \
      | docker login \
          --username AWS \
          --password-stdin ${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com

    # 4) docker-compose.yml 생성
    cat > /opt/telemetry/docker-compose.yml << 'COMPOSE'
    version: "3.8"

    services:
      loki:
        image: "${data.aws_ecr_repository.service["loki"].repository_url}:latest"
        container_name: loki
        ports:
          - "3100:3100"

      tempo:
        image: "${data.aws_ecr_repository.service["tempo"].repository_url}:latest"
        container_name: tempo
        ports:
          - "3200:3200"
          - "4317:4317"
          - "4318:4318"

      prometheus:
        image: "${data.aws_ecr_repository.service["prometheus"].repository_url}:latest"
        container_name: prometheus
        ports:
          - "9090:9090"

      grafana:
        image: "${data.aws_ecr_repository.service["grafana"].repository_url}:latest"
        container_name: grafana
        ports:
          - "3000:3000"
        environment:
          GF_SECURITY_ADMIN_USER: "admin"
          GF_SECURITY_ADMIN_PASSWORD: "admin1234"
        depends_on:
          - loki
          - tempo
          - prometheus
    COMPOSE

    # 5) 컨테이너 기동
    /usr/local/bin/docker-compose up -d
  EOF
}
