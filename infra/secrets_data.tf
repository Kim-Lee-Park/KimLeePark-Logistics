data "aws_secretsmanager_secret" "db_username" {
  name = "${var.project_name}-db-username"
}

data "aws_secretsmanager_secret_version" "db_username_current" {
  secret_id = data.aws_secretsmanager_secret.db_username.id
}

data "aws_secretsmanager_secret" "db_password" {
  name = "${var.project_name}-db-password"
}

data "aws_secretsmanager_secret_version" "db_password_current" {
  secret_id = data.aws_secretsmanager_secret.db_password.id
}

locals {
  db_username = data.aws_secretsmanager_secret_version.db_username_current.secret_string
  db_password = data.aws_secretsmanager_secret_version.db_password_current.secret_string
}
