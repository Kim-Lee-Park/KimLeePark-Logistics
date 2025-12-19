resource "aws_instance" "bastion" {
  ami                         = data.aws_ami.al2023_x86.id
  instance_type               = "t3.micro"
  key_name                    = var.ec2_key_name
  subnet_id                   = aws_subnet.public_az1.id
  vpc_security_group_ids      = [aws_security_group.bastion.id]
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

resource "null_resource" "copy_seed_files" {
  depends_on = [
    aws_instance.bastion
  ]

  triggers = {
    bastion_id = aws_instance.bastion.id
    key_hash   = filemd5(var.ec2_private_key_path)
  }

  connection {
    type        = "ssh"
    host        = aws_instance.bastion.public_ip
    user        = "ec2-user"
    private_key = file(var.ec2_private_key_path)
  }

  provisioner "file" {
    source      = var.ec2_private_key_path
    destination = "/home/ec2-user/${var.ec2_key_name}.pem"
  }

  provisioner "remote-exec" {
    inline = [
      "chown ec2-user:ec2-user /home/ec2-user/${var.ec2_key_name}.pem",
      "chmod 600 /home/ec2-user/${var.ec2_key_name}.pem",
    ]
  }
}
