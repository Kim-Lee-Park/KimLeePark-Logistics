resource "aws_instance" "kafka_zookeeper" {
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name
  subnet_id     = aws_subnet.private_kafka_az1.id

  vpc_security_group_ids = [aws_security_group.kafka.id]

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

    dnf install -y java-17-amazon-corretto wget tar

    mkdir -p /opt/kafka
    cd /opt/kafka

    if [ ! -f "kafka_2.13-3.7.0.tgz" ]; then
      wget "https://archive.apache.org/dist/kafka/3.7.0/kafka_2.13-3.7.0.tgz"
      tar -xzf "kafka_2.13-3.7.0.tgz"
      mv "kafka_2.13-3.7.0" kafka
    fi

    # ZooKeeper systemd unit
    cat >/etc/systemd/system/zookeeper.service <<'UNIT_ZK'
    [Unit]
    Description=Apache ZooKeeper Server
    After=network.target

    [Service]
    Type=simple
    User=root
    ExecStart=/opt/kafka/kafka/bin/zookeeper-server-start.sh /opt/kafka/kafka/config/zookeeper.properties
    Restart=always

    [Install]
    WantedBy=multi-user.target
    UNIT_ZK

    systemctl daemon-reload
    systemctl enable zookeeper
    systemctl start zookeeper
  EOF
}

resource "aws_instance" "kafka_broker" {
  count         = 3
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name

  subnet_id = count.index == 2 ? aws_subnet.private_kafka_az2.id : aws_subnet.private_kafka_az1.id

  vpc_security_group_ids = [aws_security_group.kafka.id]

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

    #### Install Java ####
    dnf install -y java-17-amazon-corretto wget tar

    #### Install Kafka ####
    mkdir -p /opt/kafka
    cd /opt/kafka

    if [ ! -f "kafka_2.13-3.7.0.tgz" ]; then
      wget "https://archive.apache.org/dist/kafka/3.7.0/kafka_2.13-3.7.0.tgz"
      tar -xzf "kafka_2.13-3.7.0.tgz"
      mv "kafka_2.13-3.7.0" kafka
    fi

    #### JMX Exporter ####
    mkdir -p /opt/jmx-exporter
    cd /opt/jmx-exporter

    wget -O jmx_prometheus_javaagent.jar \
      https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.20.0/jmx_prometheus_javaagent-0.20.0.jar

    cat >/opt/jmx-exporter/kafka-jmx-config.yaml <<'YAML'
    rules:
      - pattern: "kafka.server<type=BrokerTopicMetrics, name=(.+), topic=(.+)><>Count"
        name: "kafka_server_brokertopicmetrics_$1_total"
        labels:
          topic: "$2"
        type: COUNTER

      - pattern: "kafka.network<type=RequestMetrics, name=RequestsPerSec, request=(.+)><>Count"
        name: "kafka_network_requestmetrics_requests_per_sec_total"
        labels:
          request: "$1"
        type: COUNTER

      - pattern: "kafka.server<type=ReplicaManager, name=(.+)><>Value"
        name: "kafka_server_replicamanager_$1"
        type: GAUGE
    YAML

    #### Kafka 설정 수정 (broker.id, log.dirs, zookeeper.connect) ####
    # broker.id : Terraform count.index 값 그대로 사용
    sed -i "s/^broker.id=.*/broker.id=${count.index + 1}/" /opt/kafka/kafka/config/server.properties

    # 브로커별 로그 디렉토리 분리
    sed -i "s|^log.dirs=.*|log.dirs=/tmp/kafka-logs-${count.index + 1}|g" /opt/kafka/kafka/config/server.properties

    # ZooKeeper 주소는 전용 ZK 인스턴스 IP로 고정
    sed -i "s|^zookeeper.connect=.*|zookeeper.connect=${aws_instance.kafka_zookeeper.private_ip}:2181|g" /opt/kafka/kafka/config/server.properties

    # listeners / advertised.listeners 설정을 안전하게 덮어쓰기
    sed -i '/^listeners=/d' /opt/kafka/kafka/config/server.properties
    sed -i '/^advertised.listeners=/d' /opt/kafka/kafka/config/server.properties

    echo "listeners=PLAINTEXT://0.0.0.0:9092" >> /opt/kafka/kafka/config/server.properties
    echo "advertised.listeners=PLAINTEXT://$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4):9092" >> /opt/kafka/kafka/config/server.properties

    #### Kafka systemd unit ####
    cat >/etc/systemd/system/kafka.service <<'UNIT_KAFKA'
    [Unit]
    Description=Apache Kafka Server
    After=network.target

    [Service]
    Type=simple
    User=root
    ExecStart=/opt/kafka/kafka/bin/kafka-server-start.sh /opt/kafka/kafka/config/server.properties
    Environment=KAFKA_OPTS=-javaagent:/opt/jmx-exporter/jmx_prometheus_javaagent.jar=7071:/opt/jmx-exporter/kafka-jmx-config.yaml
    Restart=always

    [Install]
    WantedBy=multi-user.target
    UNIT_KAFKA

    systemctl daemon-reload
    systemctl enable kafka
    systemctl start kafka
  EOF
}

resource "aws_service_discovery_service" "kafka_otel" {
  name         = "kafka-otel"
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

resource "aws_service_discovery_instance" "kafka_otel" {
  count = length(aws_instance.kafka_otel_collector)
  service_id = aws_service_discovery_service.kafka_otel.id

  instance_id = aws_instance.kafka_otel_collector[count.index].id

  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.kafka_otel_collector[count.index].private_ip
    AWS_INSTANCE_PORT = "9496"
  }
}

resource "aws_instance" "kafka_otel_collector" {
  count         = 2
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name

  subnet_id = count.index == 1 ? aws_subnet.private_kafka_az2.id : aws_subnet.private_kafka_az1.id

  vpc_security_group_ids = [
    aws_security_group.observability_stack.id
  ]

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
  }

  tags = {
    Name = "${local.project}-kafka-otel-${count.index + 1}"
    Role = "kafka-otel-collector"
  }

  user_data = <<-EOF
    #!/bin/bash
    set -xe

    dnf update -y
    dnf install -y docker awscli
    systemctl enable docker
    systemctl start docker

    # ECR 로그인 (Terraform 값 인라인)
    aws ecr get-login-password --region "${var.aws_region}" \
      | docker login --username AWS --password-stdin \
        "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"

    mkdir -p /etc/otel

    cat > /etc/otel/otel-kafka.yaml <<CONFIG
    receivers:
      prometheus:
        config:
          scrape_configs:
            - job_name: "kafka-broker"
              static_configs:
                - targets:
                    - "${aws_instance.kafka_broker[0].private_ip}:7071"
                    - "${aws_instance.kafka_broker[1].private_ip}:7071"
                    - "${aws_instance.kafka_broker[2].private_ip}:7071"

    processors:
      batch: {}

    exporters:
      prometheus:
        endpoint: "0.0.0.0:9464"
        namespace: "kafka"
        const_labels:
          component: "kafka-otel-collector"

    service:
      pipelines:
        metrics:
          receivers: [prometheus]
          processors: [batch]
          exporters: [prometheus]
    CONFIG

    docker run -d --name kafka-otel-collector \\
      --restart always \\
      -p 9464:9464 \\
      -v /etc/otel/otel-kafka.yaml:/etc/otel-config.yaml \\
      "${local.otel_image}" \\
      --config=/etc/otel-config.yaml
  EOF
}
