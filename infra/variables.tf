variable "project_name" {
  type    = string
  default = "klp-logistics"
}

variable "environment" {
  description = "배포 환경 구분 (prod|stage)"
  type        = string
  default     = "prod"
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

variable "private_kafka_subnet_cidr_az1" {
  type    = string
  default = "10.0.5.0/24"
}

variable "private_kafka_subnet_cidr_az2" {
  type    = string
  default = "10.0.6.0/24"
}

variable "private_obs_subnet_cidr_az1" {
  type    = string
  default = "10.0.7.0/24"
}

variable "private_obs_subnet_cidr_az2" {
  type    = string
  default = "10.0.8.0/24"
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

variable "ec2_private_key_path" {
  type        = string
  description = "SSH Key Path"
}

variable "github_repository" {
  description = "GitHub repository slug (owner/repo) for OIDC 조건문 생성"
  type        = string
  default     = "Kim-Lee-Park/KimLeePark-Logistics"
}

variable "kafka_kraft_cluster_id" {
  description = "Kafka KRaft cluster.id (do not change unless rebuilding the cluster)."
  type        = string
  default     = "MkU3OEVBNTcwNTJENDM2Qk"
}
