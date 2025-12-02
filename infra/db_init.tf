resource "null_resource" "init_schemas" {
  depends_on = [
    aws_db_instance.postgres
  ]

  provisioner "local-exec" {
    environment = {
      PGPASSWORD = local.db_password
    }

    command = <<EOT
psql \
  -h ${aws_db_instance.postgres.address} \
  -p 5432 \
  -U ${local.db_username} \
  -d logistics \
  -f ./init-sql/schemas.sql
EOT
  }
}
