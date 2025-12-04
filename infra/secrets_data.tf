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

data "aws_secretsmanager_secret" "git_username" {
  name = "${var.project_name}-git-username"
}

data "aws_secretsmanager_secret_version" "git_username_current" {
  secret_id = data.aws_secretsmanager_secret.git_username.id
}

data "aws_secretsmanager_secret" "git_token" {
  name = "${var.project_name}-git-token"
}

data "aws_secretsmanager_secret_version" "git_token_current" {
  secret_id = data.aws_secretsmanager_secret.git_token.id
}

data "aws_secretsmanager_secret" "jwt_access_secret" {
  name = "${var.project_name}-jwt-access-secret"
}

data "aws_secretsmanager_secret_version" "jwt_access_secret_current" {
  secret_id = data.aws_secretsmanager_secret.jwt_access_secret.id
}

data "aws_secretsmanager_secret" "jwt_refresh_secret" {
  name = "${var.project_name}-jwt-refresh-secret"
}

data "aws_secretsmanager_secret_version" "jwt_refresh_secret_current" {
  secret_id = data.aws_secretsmanager_secret.jwt_refresh_secret.id
}

data "aws_secretsmanager_secret" "config_repo_uri" {
  name = "${var.project_name}-config-server-github-repo-uri"
}

data "aws_secretsmanager_secret_version" "config_repo_uri_current" {
  secret_id = data.aws_secretsmanager_secret.config_repo_uri.id
}

data "aws_secretsmanager_secret" "gemini_api_key" {
  name = "${var.project_name}-notification-google-gemini-api-key"
}

data "aws_secretsmanager_secret_version" "gemini_api_key_current" {
  secret_id = data.aws_secretsmanager_secret.gemini_api_key.id
}

data "aws_secretsmanager_secret" "slack_bot_token" {
  name = "${var.project_name}-notification-slack-bot-token"
}

data "aws_secretsmanager_secret_version" "slack_bot_token_current" {
  secret_id = data.aws_secretsmanager_secret.slack_bot_token.id
}

locals {
  db_username = data.aws_secretsmanager_secret_version.db_username_current.secret_string
  db_password = data.aws_secretsmanager_secret_version.db_password_current.secret_string
}
