resource "aws_instance" "kafka_broker" {
  count                       = 3
  ami                         = data.aws_ami.al2023_x86.id
  instance_type               = "t3.small"
  key_name                    = var.ec2_key_name
  user_data_replace_on_change = true

  subnet_id = count.index == 2 ? aws_subnet.private_kafka_az2.id : aws_subnet.private_kafka_az1.id

  vpc_security_group_ids = [aws_security_group.kafka.id]
  iam_instance_profile   = aws_iam_instance_profile.kafka.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
  }

  tags = {
    Name        = "${local.project}-kafka-${count.index + 1}"
    Role        = "kafka-broker"
    KafkaNodeId = tostring(count.index + 1)
  }

  user_data = <<-EOF
    #!/bin/bash
    set -xe

    dnf install -y docker nmap-ncat
    systemctl enable docker
    systemctl start docker

    AWS_REGION="${var.aws_region}"
    ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
    REPO_BASE="$${ACCOUNT_ID}.dkr.ecr.$${AWS_REGION}.amazonaws.com/${var.project_name}"

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

    # KRaft 클러스터 ID (모든 노드가 동일해야 함)
    CLUSTER_ID="${var.kafka_kraft_cluster_id}"

    MY_NODE_ID=${count.index + 1}
    CONTROLLER_VOTERS="1@kafka-1.klp.local:9093,2@kafka-2.klp.local:9093,3@kafka-3.klp.local:9093"

    for name in kafka-1.klp.local kafka-2.klp.local kafka-3.klp.local; do
      for i in $(seq 1 60); do
        if getent hosts "$${name}" >/dev/null 2>&1; then
          break
        fi
        echo "Waiting for DNS to resolve $${name}... ($i/60)"
        sleep 2
      done
    done

    echo "My Node ID: $${MY_NODE_ID}"
    echo "Controller Voters: $${CONTROLLER_VOTERS}"

    docker rm -f kafka || true
    docker run -d --name kafka \
      --restart unless-stopped \
      -p 9092:9092 \
      -p 9093:9093 \
      -e NODE_ID=$${MY_NODE_ID} \
      -e CLUSTER_ID=$${CLUSTER_ID} \
      -e CONTROLLER_QUORUM_VOTERS=$${CONTROLLER_VOTERS} \
      -e LISTENERS="PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093" \
      -e ADVERTISED_LISTENERS="PLAINTEXT://$${LOCAL_IP}:9092" \
      -e LOG_DIRS=/var/lib/kafka/data \
      -v /var/lib/kafka/data:/var/lib/kafka/data \
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

resource "aws_service_discovery_service" "kafka_broker_node" {
  count        = length(aws_instance.kafka_broker)
  name         = "kafka-${count.index + 1}"
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

resource "aws_service_discovery_instance" "kafka_broker_node" {
  count      = length(aws_instance.kafka_broker)
  service_id = aws_service_discovery_service.kafka_broker_node[count.index].id

  instance_id = "kafka-${count.index + 1}"

  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.kafka_broker[count.index].private_ip
  }
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
