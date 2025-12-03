terraform {
  required_version = ">= 1.8.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

locals {
  project = var.project_name
  az1 = "${var.aws_region}a" # Active
  az2     = "${var.aws_region}c" # Standby
}
