resource "aws_iam_role" "kafka_instance" {
  name = "${local.project}-kafka-ec2-role"

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

resource "aws_iam_role_policy_attachment" "kafka_ecr_readonly" {
  role       = aws_iam_role.kafka_instance.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_instance_profile" "kafka" {
  name = "${local.project}-kafka-instance-profile"
  role = aws_iam_role.kafka_instance.name
}
