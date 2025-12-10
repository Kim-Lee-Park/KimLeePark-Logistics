output "vpc_id" {
  value = aws_vpc.main.id
}

output "alb_dns_name" {
  value = aws_lb.public_alb.dns_name
}

output "rds_endpoints" {
  value = { for name, db in aws_db_instance.postgres : name => db.address }
}

output "redis_endpoint" {
  value = aws_elasticache_cluster.redis.configuration_endpoint
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "bastion_public_ip" {
  value = aws_instance.bastion.public_ip
}
