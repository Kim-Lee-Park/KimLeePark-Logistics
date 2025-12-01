variable "project_name" {
  type    = string
  default = "klp-logistics"
}

variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
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

variable "private_app_subnet_cidr_az2" {
  type    = string
  default = "10.0.4.0/24"
}

variable "private_db_subnet_cidr_az1" {
  type    = string
  default = "10.0.10.0/24"
}

variable "private_db_subnet_cidr_az2" {
  type    = string
  default = "10.0.20.0/24"
}

variable "allowed_ssh_cidr" {
  description = "SSH 허용 IP"
  type        = string
  default     = "0.0.0.0/0"
}

variable "ec2_key_name" {
  description = "EC2 Keypair"
  type        = string
  default     = "klp-keypair"
}
