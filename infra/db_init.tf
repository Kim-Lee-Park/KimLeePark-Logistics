resource "null_resource" "init_schemas" {
  for_each = local.db_targets

  depends_on = [
    aws_instance.bastion
  ]

  connection {
    type = "ssh"
    host = aws_instance.bastion.public_ip
    user = "ec2-user"
    private_key = file(var.ec2_private_key_path)
  }

  provisioner "file" {
    source      = "${path.root}/../init-sql/extensions.sql"
    destination = "/tmp/extensions.sql"
  }

  provisioner "remote-exec" {
    inline = [
      # 혹시 user_data에서 설치 실패했을 경우를 대비해서 한 번 더 보장
      "sudo dnf install -y postgresql15",

      # 디버깅용 로그 (원하면 삭제 가능)
      "which psql || echo 'psql not in PATH'",
      "psql --version || echo 'psql command failed'",

      # 실제 스키마 적용
      "export PGPASSWORD='${local.db_password}'",
      "psql -h ${aws_db_instance.postgres[each.key].address} -p 5432 -U ${local.db_username} -d ${each.value.db_name} -f /tmp/extensions.sql"
    ]
  }
}
