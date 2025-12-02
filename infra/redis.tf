resource "aws_elasticache_subnet_group" "redis" {
  name = "${local.project}-redis-subnet-group"
  subnet_ids = [aws_subnet.private_app_az1.id, aws_subnet.private_app_az2.id]
}

resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "${local.project}-redis"
  engine               = "redis"
  engine_version       = "7.1"
  node_type            = "cache.t4g.small"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379

  subnet_group_name = aws_elasticache_subnet_group.redis.name
  security_group_ids = [aws_security_group.redis.id]

  tags = {
    Name = "${local.project}-redis"
  }
}
