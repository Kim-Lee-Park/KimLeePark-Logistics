locals {
  db_targets = {
    auth         = { schema = "auth_schema" }
    delivery     = { schema = "delivery_schema" }
    hub          = { schema = "hub_schema" }
    notification = { schema = "notification_schema" }
    order        = { schema = "order_schema" }
    promotion    = { schema = "promotion_schema" }
    payment      = { schema = "payment_schema" }
    user         = { schema = "user_schema" }
  }
}

resource "aws_db_subnet_group" "main" {
  name = "${local.project}-db-subnet-group"
  subnet_ids = [aws_subnet.private_db_az1.id, aws_subnet.private_db_az2.id]

  tags = {
    Name = "${local.project}-db-subnet-group"
  }
}

resource "aws_db_instance" "postgres" {
  for_each = local.db_targets

  identifier        = "${local.project}-${each.key}-postgres"
  engine            = "postgres"
  engine_version    = "18.1"
  instance_class    = "db.t4g.small"
  allocated_storage = 50

  db_name  = "logistics"
  username = local.db_username
  password = local.db_password

  db_subnet_group_name = aws_db_subnet_group.main.name
  multi_az             = false
  availability_zone    = local.az1
  vpc_security_group_ids = [aws_security_group.db.id]

  backup_retention_period = 7
  skip_final_snapshot     = true
  deletion_protection     = false

  tags = {
    Name = "${local.project}-${each.key}-postgres"
  }
}
