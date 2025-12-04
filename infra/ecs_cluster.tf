resource "aws_ecs_cluster" "main" {
  name = "${local.project}-ecs-cluster"
}

resource "aws_iam_role" "ecs_task_execution" {
  name = "${local.project}-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = { Service = "ecs-tasks.amazonaws.com" }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_policy" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "ecs_task_role" {
  name = "${local.project}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = { Service = "ecs-tasks.amazonaws.com" }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_policy" "ecs_exec_secrets" {
  name = "${local.project}-ecs-exec-secrets"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ],
        Resource = [
          data.aws_secretsmanager_secret.db_username.arn,
          data.aws_secretsmanager_secret.db_password.arn,
          data.aws_secretsmanager_secret.config_repo_uri.arn,
          data.aws_secretsmanager_secret.git_username.arn,
          data.aws_secretsmanager_secret.git_token.arn,
          data.aws_secretsmanager_secret.jwt_access_secret.arn,
          data.aws_secretsmanager_secret.jwt_refresh_secret.arn,
          data.aws_secretsmanager_secret.gemini_api_key.arn,
          data.aws_secretsmanager_secret.slack_bot_token.arn
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_secrets" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = aws_iam_policy.ecs_exec_secrets.arn
}
