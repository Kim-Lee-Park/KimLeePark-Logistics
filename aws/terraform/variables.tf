variable "region" {
  type        = string
  description = "AWS region"
  default     = "ap-northeast-2"
}

variable "project_name" {
  type        = string
  default     = "deploy-study"
}

variable "vpc_cidr" {
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr_az1" {
  type    = string
  default = "10.0.1.0/24"
}

variable "public_subnet_cidr_az2" {
  type    = string
  default = "10.0.3.0/24"
}

variable "private_app_subnet_cidr_az1" {
  type    = string
  default = "10.0.2.0/24"
}

variable "private_db_subnet_cidr_az1" {
  type    = string
  default = "10.0.10.0/24"
}

variable "private_db_subnet_cidr_az2" {
  type    = string
  default = "10.0.20.0/24"
}

variable "order_image" {
  type        = string
  description = "Order 서비스용 ECR 이미지 URI"
}

variable "payment_image" {
  type        = string
  description = "Payment 서비스용 ECR 이미지 URI"
}

variable "product_image" {
  type        = string
  description = "Product 서비스용 ECR 이미지 URI"
}

variable "rds_master_username" {
  type        = string
  default     = "appuser"
}

variable "rds_master_password" {
  type        = string
  sensitive   = true
}

variable "rds_allocated_storage" {
  type    = number
  default = 20
}

variable "order_desired_count" {
  type    = number
  default = 2
}

variable "payment_desired_count" {
  type    = number
  default = 1
}

variable "product_desired_count" {
  type    = number
  default = 1
}
