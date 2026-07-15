#!/usr/bin/env bash
# ==============================================================================
# LandLens Programmatic AWS ECS Fargate Deployment Automation Script
# ==============================================================================
set -euo pipefail

# Configurations
AWS_REGION="ap-south-1"
echo "Fetching AWS Account ID..."
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text | tr -d '\r\n')
if [ -z "$AWS_ACCOUNT_ID" ]; then
  echo "ERROR: Could not retrieve AWS Account ID. Please verify your AWS credentials." >&2
  exit 1
fi
echo "Using AWS Account ID: $AWS_ACCOUNT_ID"
PROJECT_NAME="landlens"
ENVIRONMENT="production"
ECR_REPO_NAME="landlens-backend"
CONTAINER_NAME="landlens-backend-container"

echo "======================================================================"
echo " Starting LandLens Production Deployment to AWS ECS Fargate"
echo "======================================================================"

# Helper function to check command dependency
check_dependency() {
  if ! command -v "$1" &> /dev/null; then
    echo "ERROR: Required tool '$1' is not installed or not in PATH." >&2
    exit 1
  fi
}

# 1. Check prerequisites
echo "[1/9] Checking tool dependencies..."
check_dependency "aws"
# Check Java/Docker/Terraform but allow overrides or warning
if ! command -v "./mvnw" &> /dev/null && ! command -v "mvn" &> /dev/null; then
  echo "WARNING: Maven compiler wrapper not found. Ensure you run this from root."
fi
if ! command -v "docker" &> /dev/null; then
  echo "ERROR: Docker CLI is not running. Docker is required to build containers." >&2
  exit 1
fi
if ! command -v "terraform" &> /dev/null; then
  echo "ERROR: Terraform CLI is not found. Please install Terraform first." >&2
  exit 1
fi

# 2. Build Spring Boot JAR
echo "[2/9] Compiling and packaging Spring Boot Application..."
if [ -f "./mvnw" ]; then
  ./mvnw clean package -DskipTests
elif [ -f "./mvnw.cmd" ]; then
  # On Windows Git Bash compatibility
  ./mvnw.cmd clean package -DskipTests
else
  mvn clean package -DskipTests
fi
echo "Spring Boot packaged successfully."

# 3. Authenticate with Amazon ECR
echo "[3/9] Logging in to Amazon ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

# 4. Create ECR repository if it does not exist
echo "[4/9] Verifying Amazon ECR repository..."
if ! aws ecr describe-repositories --repository-names "$ECR_REPO_NAME" --region "$AWS_REGION" &>/dev/null; then
  echo "ECR Repository '$ECR_REPO_NAME' not found. Creating repository..."
  aws ecr create-repository --repository-name "$ECR_REPO_NAME" --region "$AWS_REGION"
fi
ECR_URL="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME"

# 5. Build and tag Docker Image
echo "[5/9] Building and tagging Docker image..."
docker build -t "$ECR_REPO_NAME:latest" .
docker tag "$ECR_REPO_NAME:latest" "$ECR_URL:latest"

# 6. Push image to Amazon ECR
echo "[6/9] Pushing image to ECR..."
docker push "$ECR_URL:latest"
echo "Image pushed successfully: $ECR_URL:latest"

# 7. Apply Terraform Infrastructure
echo "[7/9] Initializing and applying Terraform templates..."
cd terraform
terraform init
terraform apply -auto-approve

# Extract outputs
ALB_DNS_NAME=$(terraform output -raw alb_dns_name)
ECS_CLUSTER=$(terraform output -raw ecs_cluster_name)
ECS_SERVICE=$(terraform output -raw ecs_service_name)
cd ..

echo "Terraform infrastructure configured. ALB URL: http://$ALB_DNS_NAME"

# 8. Force ECS deployment to load the new image
echo "[8/9] Triggering ECS service redeployment..."
aws ecs update-service --cluster "$ECS_CLUSTER" --service "$ECS_SERVICE" --force-new-deployment --region "$AWS_REGION"

echo "Waiting for ECS Fargate deployment to stabilize..."
aws ecs wait services-stable --cluster "$ECS_CLUSTER" --services "$ECS_SERVICE" --region "$AWS_REGION"
echo "ECS Service is stable and running."

# 9. Verify deployment health check
echo "[9/9] Verifying Actuator Health Check..."
BASE_URL="http://$ALB_DNS_NAME"
HEALTH_CHECK_URL="$BASE_URL/actuator/health"
echo "Querying: $HEALTH_CHECK_URL"

# Poll health check
for i in {1..20}; do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_CHECK_URL" || echo "000")
  if [ "$HTTP_STATUS" -eq 200 ]; then
    HEALTH_BODY=$(curl -s "$HEALTH_CHECK_URL")
    if echo "$HEALTH_BODY" | grep -q "UP"; then
      echo "======================================================================"
      echo " DEPLOYMENT COMPLETED SUCCESSFULLY!"
      echo "======================================================================"
      echo "Application Base URL:  $BASE_URL"
      echo "Swagger UI Endpoint:   $BASE_URL/swagger-ui/index.html"
      echo "Actuator Health:       $HEALTH_CHECK_URL"
      echo "API Endpoints Docs:    $BASE_URL/v3/api-docs"
      echo "======================================================================"
      exit 0
    fi
  fi
  echo "Waiting for app to start up and report healthy (HTTP Status: $HTTP_STATUS) [retry $i/20]..."
  sleep 10
done

echo "WARNING: Deployment succeeded but health check did not return HTTP 200/UP within the timeout window."
echo "Please check container logs in CloudWatch log group: /ecs/$PROJECT_NAME-$ENVIRONMENT-backend"
exit 1
