# ALB Public DNS Name
output "alb_dns_name" {
  value       = aws_lb.main.dns_name
  description = "Application load balancer public DNS address"
}

# ECR URL
output "ecr_repository_url" {
  value       = aws_ecr_repository.app.repository_url
  description = "ECR Docker registry url to push images"
}

# ECS Info
output "ecs_cluster_name" {
  value       = aws_ecs_cluster.main.name
  description = "ECS Cluster Name"
}

output "ecs_service_name" {
  value       = aws_ecs_service.main.name
  description = "ECS Service Name"
}

# Database Secrets Manager ARN
output "secrets_manager_db_arn" {
  value       = aws_secretsmanager_secret.db_secret.arn
  description = "Database Credentials Secrets Manager Secret ARN"
}

output "secrets_manager_jwt_arn" {
  value       = aws_secretsmanager_secret.jwt_secret.arn
  description = "JWT Secrets Manager Secret ARN"
}

output "codebuild_bucket_name" {
  value       = aws_s3_bucket.codebuild_source.id
  description = "CodeBuild source S3 bucket name"
}

output "codebuild_project_name" {
  value       = aws_codebuild_project.app.name
  description = "CodeBuild project name"
}
