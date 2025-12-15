locals {
  db_targets = {
    auth = { db_name = "auth" }
    delivery = { db_name = "delivery" }
    hub = { db_name = "hub" }
    notification = { db_name = "notification" }
    order = { db_name = "orders" }
    promotion = { db_name = "promotion" }
    payment = { db_name = "payment" }
    user = { db_name = "users" }
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

  db_name  = each.value.db_name
  username = local.db_username
  password = local.db_password

  db_subnet_group_name = aws_db_subnet_group.main.name
  multi_az             = false
  availability_zone    = local.az1
  vpc_security_group_ids = [aws_security_group.db.id]

  backup_retention_period = 0
  skip_final_snapshot     = true
  deletion_protection     = false

  tags = {
    Name = "${local.project}-${each.key}-postgres"
  }
}
