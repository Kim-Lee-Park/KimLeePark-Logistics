resource "aws_instance" "bastion" {
  ami                         = data.aws_ami.al2023_x86.id
  instance_type               = "t3.micro"
  key_name                    = var.ec2_key_name
  subnet_id                   = aws_subnet.public_az1.id
  vpc_security_group_ids = [aws_security_group.bastion.id]
  associate_public_ip_address = true

  user_data = <<-EOF
    #!/bin/bash
    set -xe

    dnf update -y

    dnf install -y postgresql15
  EOF

  tags = {
    Name = "${local.project}-bastion"
  }
}
