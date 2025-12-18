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

resource "aws_instance" "observability_metrics" {
  ami                    = data.aws_ami.al2023_x86.id
  instance_type          = "t3.medium"
  key_name               = var.ec2_key_name
  subnet_id              = aws_subnet.private_obs_az1.id
  vpc_security_group_ids = [aws_security_group.observability_metrics.id]

  iam_instance_profile = aws_iam_instance_profile.obs.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 100
  }

  tags = {
    Name = "${local.project}-obs-metrics"
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

    # 4) docker-compose.yml 생성 (Prometheus + Grafana)
    cat > /opt/telemetry/docker-compose.yml << 'COMPOSE'
    services:
      prometheus:
        image: "${data.aws_ecr_repository.service["prometheus"].repository_url}:latest"
        container_name: prometheus
        command:
          - "--enable-feature=remote-write-receiver"
          - "--web.enable-remote-write-receiver"
        ports:
          - "9090:9090"

      grafana:
        image: "${data.aws_ecr_repository.service["grafana"].repository_url}:latest"
        container_name: grafana
        environment:
          GF_SECURITY_ADMIN_USER: "admin"
          GF_SECURITY_ADMIN_PASSWORD: "admin1234"
          GF_SERVER_ROOT_URL: "%(protocol)s://%(domain)s/grafana"
          GF_SERVER_SERVE_FROM_SUB_PATH: "true"
          GF_PATHS_PROVISIONING: "/etc/grafana/provisioning"
        ports:
          - "3000:3000"
        depends_on:
          - prometheus
    COMPOSE

    # 5) 컨테이너 기동
    /usr/local/bin/docker-compose up -d
  EOF
}

resource "aws_instance" "observability_telemetry" {
  ami                    = data.aws_ami.al2023_x86.id
  instance_type          = "t3.large"
  key_name               = var.ec2_key_name
  subnet_id              = aws_subnet.private_obs_az2.id
  vpc_security_group_ids = [aws_security_group.observability_telemetry.id]

  iam_instance_profile = aws_iam_instance_profile.obs.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 100
  }

  tags = {
    Name = "${local.project}-obs-telemetry"
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

    # 4) docker-compose.yml 생성 (Loki + Tempo + OTel Collector)
    cat > /opt/telemetry/docker-compose.yml << 'COMPOSE'
    version: "3.8"

    services:
      tempo:
        image: "${data.aws_ecr_repository.service["tempo"].repository_url}:latest"
        container_name: tempo
        ports:
          - "3200:3200"

      loki:
        image: "${data.aws_ecr_repository.service["loki"].repository_url}:latest"
        container_name: loki
        ports:
          - "3100:3100"

      otel-collector:
        image: "${data.aws_ecr_repository.service["otel-collector"].repository_url}:latest"
        container_name: otel-collector
        environment:
          TEMPO_HOST: tempo
          LOKI_HOST: loki
        ports:
          - "4317:4317"
          - "4318:4318"
          - "9464:9464"
        depends_on:
          - tempo
          - loki
    COMPOSE

    # 5) 컨테이너 기동
    /usr/local/bin/docker-compose up -d
  EOF
}

resource "aws_service_discovery_service" "loki" {
  name         = "loki"
  namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

    dns_records {
      ttl  = 10
      type = "A"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_service_discovery_instance" "loki" {
  service_id  = aws_service_discovery_service.loki.id
  instance_id = aws_instance.observability_telemetry.id
  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.observability_telemetry.private_ip
  }
}

resource "aws_service_discovery_service" "tempo" {
  name         = "tempo"
  namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

    dns_records {
      ttl  = 10
      type = "A"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_service_discovery_instance" "tempo" {
  service_id  = aws_service_discovery_service.tempo.id
  instance_id = aws_instance.observability_telemetry.id
  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.observability_telemetry.private_ip
  }
}

resource "aws_service_discovery_service" "prometheus" {
  name         = "prometheus"
  namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

    dns_records {
      ttl  = 10
      type = "A"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_service_discovery_instance" "prometheus" {
  service_id  = aws_service_discovery_service.prometheus.id
  instance_id = aws_instance.observability_metrics.id
  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.observability_metrics.private_ip
  }
}
