# Random string for unique S3 bucket naming
resource "random_string" "bucket_suffix" {
  length  = 8
  special = false
  upper   = false
}

# S3 Bucket for storing the zipped project source code
resource "aws_s3_bucket" "codebuild_source" {
  bucket        = "${var.project_name}-${var.environment}-build-source-${random_string.bucket_suffix.result}"
  force_destroy = true
}

# IAM Role for AWS CodeBuild
resource "aws_iam_role" "codebuild" {
  name = "${var.project_name}-${var.environment}-codebuild-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "codebuild.amazonaws.com"
        }
      }
    ]
  })
}

# IAM Policy for AWS CodeBuild
resource "aws_iam_role_policy" "codebuild" {
  name = "${var.project_name}-${var.environment}-codebuild-policy"
  role = aws_iam_role.codebuild.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:GetObjectVersion",
          "s3:PutObject"
        ]
        Resource = "${aws_s3_bucket.codebuild_source.arn}/*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:PutImage"
        ]
        Resource = aws_ecr_repository.app.arn
      }
    ]
  })
}

# AWS CodeBuild Project Configuration
resource "aws_codebuild_project" "app" {
  name          = "${var.project_name}-${var.environment}-build"
  description   = "Builds the LandLens backend Docker container and pushes to ECR"
  build_timeout = 20
  service_role  = aws_iam_role.codebuild.arn

  artifacts {
    type = "NO_ARTIFACTS"
  }

  environment {
    compute_type                = "BUILD_GENERAL1_SMALL"
    image                       = "aws/codebuild/amazonlinux2-x86_64-standard:5.0"
    type                        = "LINUX_CONTAINER"
    image_pull_credentials_type = "CODEBUILD"
    privileged_mode             = true

    environment_variable {
      name  = "AWS_REGION"
      value = var.aws_region
    }

    environment_variable {
      name  = "IMAGE_REPO_NAME"
      value = aws_ecr_repository.app.name
    }

    environment_variable {
      name  = "CONTAINER_NAME"
      value = "${var.project_name}-backend-container"
    }
  }

  source {
    type      = "S3"
    location  = "${aws_s3_bucket.codebuild_source.id}/source.zip"
    buildspec = "buildspec.yml"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-codebuild"
  }
}
