resource "aws_instance" "kafka_zookeeper" {
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name
  subnet_id     = aws_subnet.private_kafka_az1.id

  vpc_security_group_ids = [aws_security_group.kafka.id]
  iam_instance_profile   = aws_iam_instance_profile.kafka.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
  }

  tags = {
    Name = "${local.project}-kafka-zookeeper"
    Role = "kafka-zookeeper"
  }

  user_data = <<-EOF
    #!/bin/bash
    set -xe

    dnf install -y docker nmap-ncat
    systemctl enable docker
    systemctl start docker

    AWS_REGION="${var.aws_region}"
    ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
    REPO_BASE="$${ACCOUNT_ID}.dkr.ecr.$${AWS_REGION}.amazonaws.com/${local.project}"

    # ECR login (uses VPC endpoints + instance profile)
    aws ecr get-login-password --region "$${AWS_REGION}" \
      | docker login --username AWS --password-stdin "$${ACCOUNT_ID}.dkr.ecr.$${AWS_REGION}.amazonaws.com"

    # pull zk image
    docker pull "$${REPO_BASE}-kafka-zookeeper:latest"

    # run zookeeper
    docker rm -f zookeeper || true
    docker run -d --name zookeeper \
      --restart unless-stopped \
      -p 2181:2181 \
      -e DATA_DIR=/var/lib/zookeeper/data \
      -e DATA_LOG_DIR=/var/lib/zookeeper/log \
      -e CLIENT_PORT=2181 \
      -v /var/lib/zookeeper/data:/var/lib/zookeeper/data \
      -v /var/lib/zookeeper/log:/var/lib/zookeeper/log \
      "$${REPO_BASE}-kafka-zookeeper:latest"
  EOF
}

resource "aws_instance" "kafka_broker" {
  count         = 3
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name

  subnet_id = count.index == 2 ? aws_subnet.private_kafka_az2.id : aws_subnet.private_kafka_az1.id

  vpc_security_group_ids = [aws_security_group.kafka.id]
  iam_instance_profile   = aws_iam_instance_profile.kafka.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
  }

  tags = {
    Name = "${local.project}-kafka-${count.index + 1}"
    Role = "kafka-broker"
  }

  depends_on = [aws_instance.kafka_zookeeper]

  user_data = <<-EOF
    #!/bin/bash
    set -xe

    dnf install -y docker
    systemctl enable docker
    systemctl start docker

    AWS_REGION="${var.aws_region}"
    ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
    REPO_BASE="$${ACCOUNT_ID}.dkr.ecr.$${AWS_REGION}.amazonaws.com/${local.project}"

    aws ecr get-login-password --region "$${AWS_REGION}" \
      | docker login --username AWS --password-stdin "$${ACCOUNT_ID}.dkr.ecr.$${AWS_REGION}.amazonaws.com"

    # pull images
    docker pull "$${REPO_BASE}-kafka:latest"
    docker pull "$${REPO_BASE}-kafka-exporter:latest"

    TOKEN="$(curl -s -X PUT \"http://169.254.169.254/latest/api/token\" -H \"X-aws-ec2-metadata-token-ttl-seconds: 21600\" || true)"
    LOCAL_IP="$(curl -s -H \"X-aws-ec2-metadata-token: $${TOKEN}\" http://169.254.169.254/latest/meta-data/local-ipv4 || true)"
    if [ -z "$${LOCAL_IP}" ]; then
      LOCAL_IP="$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4 || true)"
    fi
    if [ -z "$${LOCAL_IP}" ]; then
      LOCAL_IP="$(hostname -I | awk '{print $1}')"
    fi
    echo "LOCAL_IP resolved to $${LOCAL_IP}"

    docker rm -f kafka || true
    docker run -d --name kafka \
      --restart unless-stopped \
      -p 9092:9092 \
      -e BROKER_ID=${count.index + 1} \
      -e ZOOKEEPER_CONNECT=${aws_instance.kafka_zookeeper.private_ip}:2181 \
      -e LISTENERS=PLAINTEXT://0.0.0.0:9092 \
      -e ADVERTISED_LISTENERS=PLAINTEXT://$${LOCAL_IP}:9092 \
      -e LOG_DIRS=/var/lib/kafka/data-${count.index + 1} \
      -v /var/lib/kafka/data-${count.index + 1}:/var/lib/kafka/data-${count.index + 1} \
      "$${REPO_BASE}-kafka:latest"

    docker rm -f kafka-exporter || true
    for i in $(seq 1 30); do
      if nc -z "$${LOCAL_IP}" 9092; then
        break
      fi
      echo "Waiting for Kafka to listen on 9092... ($i/30)"
      sleep 2
    done
    docker run -d --name kafka-exporter \
      --restart unless-stopped \
      --network host \
      "$${REPO_BASE}-kafka-exporter:latest" \
      --kafka.server=$${LOCAL_IP}:9092
  EOF
}

resource "aws_service_discovery_service" "kafka_exporter" {
  name         = "kafka-exporter"
  namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.ecs.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "WEIGHTED"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_service_discovery_instance" "kafka_exporter" {
  count      = length(aws_instance.kafka_broker)
  service_id = aws_service_discovery_service.kafka_exporter.id

  instance_id = aws_instance.kafka_broker[count.index].id

  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.kafka_broker[count.index].private_ip
    AWS_INSTANCE_PORT = "9308"
  }
}
