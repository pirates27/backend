# Database Credentials Secret
resource "aws_secretsmanager_secret" "db_secret" {
  name                    = "${var.project_name}-${var.environment}-db-credentials"
  description             = "Database configuration credentials for LandLens app"
  recovery_window_in_days = 0
}

# Initial placeholder values for Database Secret (which should be updated manually or via CLI)
resource "aws_secretsmanager_secret_version" "db_initial" {
  secret_id     = aws_secretsmanager_secret.db_secret.id
  secret_string = jsonencode({
    db_url      = "jdbc:mysql://srv1117.hstgr.io:3306/u833088220_land_lens?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useInformationSchema=true"
    db_username = "u833088220_admin"
    db_password = "Land_lens_1"
  })
  lifecycle {
    ignore_changes = [secret_string] # Prevent Terraform from overwriting updates made in the AWS console
  }
}

# JWT Config Secret
resource "aws_secretsmanager_secret" "jwt_secret" {
  name                    = "${var.project_name}-${var.environment}-jwt-secret"
  description             = "JWT secret configuration for LandLens app"
  recovery_window_in_days = 0
}

# Initial placeholder value for JWT Secret
resource "aws_secretsmanager_secret_version" "jwt_initial" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = jsonencode({
    jwt_secret  = "9a2f3f4e5d6c7b8a9f0e1d2c3b4a5f6e7d8c9b0a1f2e3d4c5b6a7f8e9d0c1b2a3"
  })
  lifecycle {
    ignore_changes = [secret_string]
  }
}
