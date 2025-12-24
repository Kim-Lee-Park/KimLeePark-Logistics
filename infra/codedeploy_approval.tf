locals {
  approval_lambda_runtime = "nodejs20.x"
}

resource "aws_dynamodb_table" "codedeploy_approval" {
  name         = "${local.project}-codedeploy-approval"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "deploymentId"

  attribute {
    name = "deploymentId"
    type = "S"
  }
}

data "archive_file" "codedeploy_approval_request" {
  type        = "zip"
  source_file = "${path.module}/lambda/codedeploy_approval_request.js"
  output_path = "${path.module}/lambda/codedeploy_approval_request.zip"
}

data "archive_file" "codedeploy_approval_callback" {
  type        = "zip"
  source_file = "${path.module}/lambda/codedeploy_approval_callback.js"
  output_path = "${path.module}/lambda/codedeploy_approval_callback.zip"
}

resource "aws_iam_role" "codedeploy_approval_lambda" {
  name = "${local.project}-codedeploy-approval-lambda"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = { Service = "lambda.amazonaws.com" }
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "codedeploy_approval_logs" {
  role       = aws_iam_role.codedeploy_approval_lambda.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "codedeploy_approval_codedeploy" {
  name = "${local.project}-codedeploy-approval-codedeploy"
  role = aws_iam_role.codedeploy_approval_lambda.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "codedeploy:PutLifecycleEventHookExecutionStatus",
          "codedeploy:GetDeployment",
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy" "codedeploy_approval_dynamodb" {
  name = "${local.project}-codedeploy-approval-dynamodb"
  role = aws_iam_role.codedeploy_approval_lambda.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action = ["dynamodb:PutItem", "dynamodb:GetItem", "dynamodb:UpdateItem"]
        Resource = aws_dynamodb_table.codedeploy_approval.arn
      }
    ]
  })
}

resource "aws_lambda_function" "codedeploy_approval_request" {
  function_name = "${local.project}-codedeploy-approval-request"
  role          = aws_iam_role.codedeploy_approval_lambda.arn
  handler       = "codedeploy_approval_request.handler"
  runtime       = local.approval_lambda_runtime
  filename      = data.archive_file.codedeploy_approval_request.output_path

  environment {
    variables = {
      SLACK_WEBHOOK_URL           = data.aws_secretsmanager_secret_version.codedeploy_slack_webhook.secret_string
      SERVICE_NAME                = local.project
      CODEDEPLOY_APP              = local.project
      CODEDEPLOY_DEPLOYMENT_GROUP = "${local.project}-ecs"
      TABLE_NAME                  = aws_dynamodb_table.codedeploy_approval.name
    }
  }
}

resource "aws_lambda_function" "codedeploy_approval_callback" {
  function_name = "${local.project}-codedeploy-approval-callback"
  role          = aws_iam_role.codedeploy_approval_lambda.arn
  handler       = "codedeploy_approval_callback.handler"
  runtime       = local.approval_lambda_runtime
  filename      = data.archive_file.codedeploy_approval_callback.output_path

  environment {
    variables = {
      TABLE_NAME = aws_dynamodb_table.codedeploy_approval.name
    }
  }
}

data "aws_secretsmanager_secret_version" "codedeploy_slack_webhook" {
  secret_id = "${var.project_name}-codedeploy-approval-slack-webhook"
}

resource "aws_apigatewayv2_api" "codedeploy_approval" {
  name          = "${local.project}-approval-api"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_integration" "codedeploy_approval" {
  api_id                 = aws_apigatewayv2_api.codedeploy_approval.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.codedeploy_approval_callback.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "codedeploy_approval" {
  api_id    = aws_apigatewayv2_api.codedeploy_approval.id
  route_key = "POST /slack/approval"
  target    = "integrations/${aws_apigatewayv2_integration.codedeploy_approval.id}"
}

resource "aws_apigatewayv2_stage" "codedeploy_approval" {
  api_id      = aws_apigatewayv2_api.codedeploy_approval.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_lambda_permission" "codedeploy_approval_api" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.codedeploy_approval_callback.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.codedeploy_approval.execution_arn}/*/*"
}

output "codedeploy_approval_request_lambda_arn" {
  value       = aws_lambda_function.codedeploy_approval_request.arn
  description = "Lambda ARN to use in CodeDeploy BeforeAllowTraffic hook"
}

output "codedeploy_approval_callback_url" {
  value       = "${aws_apigatewayv2_api.codedeploy_approval.api_endpoint}/slack/approval"
  description = "API Gateway URL for Slack Interactivity Request URL"
}
