output "alb_dns_name" {
  description = "ALB DNS Name"
  value       = aws_lb.this.dns_name
}

output "rds_endpoint" {
  description = "RDS Endpoint"
  value       = aws_db_instance.rds.address
}
