resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${local.project}-vpc"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${local.project}-igw"
  }
}

resource "aws_subnet" "public_az1" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr_az1
  availability_zone       = local.az1
  map_public_ip_on_launch = true

  tags = {
    Name = "${local.project}-public-az1"
  }
}

resource "aws_subnet" "public_az2" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr_az2
  availability_zone       = local.az2
  map_public_ip_on_launch = true

  tags = {
    Name = "${local.project}-public-az2"
  }
}

# App Subnet
resource "aws_subnet" "private_app_az1" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_app_subnet_cidr_az1
  availability_zone = local.az1

  tags = {
    Name = "${local.project}-private-app-az1"
  }
}

resource "aws_subnet" "private_app_az2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_app_subnet_cidr_az2
  availability_zone = local.az2

  tags = {
    Name = "${local.project}-private-app-az2"
  }
}

# Kafka Subnet
resource "aws_subnet" "private_kafka_az1" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_kafka_subnet_cidr_az1
  availability_zone = local.az1

  tags = {
    Name = "${local.project}-private-kafka-az1"
  }
}

resource "aws_subnet" "private_kafka_az2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_kafka_subnet_cidr_az2
  availability_zone = local.az2

  tags = {
    Name = "${local.project}-private-kafka-az2"
  }
}

# Observability Stack Subnet
resource "aws_subnet" "private_obs_az1" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_obs_subnet_cidr_az1
  availability_zone = local.az1

  tags = {
    Name = "${local.project}-private-obs-az1"
  }
}

resource "aws_subnet" "private_obs_az2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_obs_subnet_cidr_az2
  availability_zone = local.az2

  tags = {
    Name = "${local.project}-private-obs-az2"
  }
}

# DB Subnet
resource "aws_subnet" "private_db_az1" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_db_subnet_cidr_az1
  availability_zone = local.az1

  tags = {
    Name = "${local.project}-private-db-az1"
  }
}

resource "aws_subnet" "private_db_az2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_db_subnet_cidr_az2
  availability_zone = local.az2

  tags = {
    Name = "${local.project}-private-db-az2"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "${local.project}-public-rtb"
  }
}

resource "aws_route_table_association" "public_az1" {
  subnet_id      = aws_subnet.public_az1.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_az2" {
  subnet_id      = aws_subnet.public_az2.id
  route_table_id = aws_route_table.public.id
}

resource "aws_eip" "nat_eip" {
  tags = {
    Name = "${local.project}-nat-eip"
  }
}

resource "aws_nat_gateway" "nat_az1" {
  allocation_id = aws_eip.nat_eip.id
  subnet_id     = aws_subnet.public_az1.id
  depends_on = [aws_internet_gateway.igw]

  tags = {
    Name = "${local.project}-nat-az1"
  }
}

resource "aws_route_table" "private_app" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat_az1.id
  }

  tags = {
    Name = "${local.project}-private-app-rtb"
  }
}

resource "aws_route_table_association" "private_app_az1" {
  subnet_id      = aws_subnet.private_app_az1.id
  route_table_id = aws_route_table.private_app.id
}

resource "aws_route_table_association" "private_app_az2" {
  subnet_id      = aws_subnet.private_app_az2.id
  route_table_id = aws_route_table.private_app.id
}

resource "aws_route_table_association" "private_kafka_az1" {
  subnet_id      = aws_subnet.private_kafka_az1.id
  route_table_id = aws_route_table.private_app.id
}

resource "aws_route_table_association" "private_kafka_az2" {
  subnet_id      = aws_subnet.private_kafka_az2.id
  route_table_id = aws_route_table.private_app.id
}

resource "aws_route_table_association" "private_obs_az1" {
  subnet_id      = aws_subnet.private_obs_az1.id
  route_table_id = aws_route_table.private_app.id
}

resource "aws_route_table_association" "private_obs_az2" {
  subnet_id      = aws_subnet.private_obs_az2.id
  route_table_id = aws_route_table.private_app.id
}

resource "aws_route_table" "private_db" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat_az1.id
  }

  tags = {
    Name = "${local.project}-private-db-rtb"
  }
}

resource "aws_route_table_association" "private_db_az1" {
  subnet_id      = aws_subnet.private_db_az1.id
  route_table_id = aws_route_table.private_db.id
}

resource "aws_route_table_association" "private_db_az2" {
  subnet_id      = aws_subnet.private_db_az2.id
  route_table_id = aws_route_table.private_db.id
}
