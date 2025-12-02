resource "aws_instance" "kafka_broker" {
  count         = 3
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name

  subnet_id = count.index == 2 ? aws_subnet.private_app_az2.id : aws_subnet.private_app_az1.id

  vpc_security_group_ids = [aws_security_group.kafka.id]

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
  }

  tags = {
    Name = "${local.project}-kafka-${count.index + 1}"
    Role = "kafka-broker"
  }
}
