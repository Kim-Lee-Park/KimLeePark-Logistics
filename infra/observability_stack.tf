resource "aws_instance" "observability_stack" {
  ami           = data.aws_ami.al2023_x86.id
  instance_type = "t3.small"
  key_name      = var.ec2_key_name
  subnet_id     = aws_subnet.private_app_az1.id
  vpc_security_group_ids = [aws_security_group.observability_stack.id]

  root_block_device {
    volume_type = "gp3"
    volume_size = 100
  }

  tags = {
    Name = "${local.project}-obs"
  }
}
