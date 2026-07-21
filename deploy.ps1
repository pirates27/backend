# ==============================================================================
# LandLens Windows PowerShell Direct Deployment Automation Script
# ==============================================================================
# Add current workspace directory to PATH
$scriptDir = $PSScriptRoot
if ($null -eq $scriptDir -or $scriptDir -eq "") {
    $scriptDir = Get-Location
}
$env:PATH = "$scriptDir;$env:PATH"

# Configurations
$awsRegion = "ap-south-1"
Write-Host "Fetching AWS Account ID..." -ForegroundColor Yellow
$awsAccountId = (aws sts get-caller-identity --query Account --output text).Trim()
if ($null -eq $awsAccountId -or $awsAccountId -eq "") {
    Write-Error "Could not retrieve AWS Account ID. Please verify your AWS credentials."
    exit 1
}
Write-Host "Using AWS Account ID: $awsAccountId" -ForegroundColor Green

$projectName = "landlens"
$environment = "production"
$ecrRepoName = "landlens-backend"
$ecsCluster = "landlens-production-cluster"
$ecsService = "landlens-production-service"
$albDnsName = "landlens-production-alb-1919392235.ap-south-1.elb.amazonaws.com"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Starting LandLens Direct Production Deployment to AWS ECS Fargate (Windows)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Dependency Checks
Write-Host "[1/7] Checking tool dependencies..." -ForegroundColor Yellow
$dependencies = @("aws", "docker", "java")
foreach ($dep in $dependencies) {
    if (-not (Get-Command $dep -ErrorAction SilentlyContinue)) {
        Write-Error "Required tool '$dep' is not installed or not in your system environment PATH. Please install it to run this script."
        exit 1
    }
}

# 2. Authenticate Docker with AWS ECR
Write-Host "[2/7] Authenticating Docker to AWS ECR..." -ForegroundColor Yellow
$ecrUrl = "$awsAccountId.dkr.ecr.$awsRegion.amazonaws.com"
aws ecr get-login-password --region $awsRegion | docker login --username AWS --password-stdin $ecrUrl
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to authenticate with AWS ECR."
    exit 1
}

# 3. Build JAR File locally with Maven Wrapper
Write-Host "[3/7] Building JAR with Maven..." -ForegroundColor Yellow
& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed."
    exit 1
}

# 4. Build Docker Image
Write-Host "[4/7] Building Docker Image..." -ForegroundColor Yellow
docker build -t $ecrRepoName .
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker build failed."
    exit 1
}

# 5. Tag and Push to ECR
Write-Host "[5/7] Tagging and Pushing to ECR..." -ForegroundColor Yellow
$imageUri = "$ecrUrl/$ecrRepoName:latest"
docker tag "${ecrRepoName}:latest" $imageUri
docker push $imageUri
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to push image to ECR."
    exit 1
}

# 6. Force ECS Deployment
Write-Host "[6/7] Triggering ECS service redeployment..." -ForegroundColor Yellow
aws ecs update-service --cluster $ecsCluster --service $ecsService --force-new-deployment --region $awsRegion | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to trigger ECS deployment."
    exit 1
}

# 7. Wait for ECS Fargate Deployment
Write-Host "[7/7] Waiting for ECS Fargate deployment to stabilize (this may take a few minutes)..." -ForegroundColor Yellow
aws ecs wait services-stable --cluster $ecsCluster --services $ecsService --region $awsRegion
Write-Host "ECS Service is stable and running." -ForegroundColor Green

# Verify Health Check
Write-Host "`nVerifying Actuator Health Check..." -ForegroundColor Yellow
$baseUrl = "http://$albDnsName"
$healthUrl = "$baseUrl/actuator/health"
Write-Host "Querying: $healthUrl" -ForegroundColor Cyan

for ($i = 1; $i -le 20; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 5
        if ($response.status -eq "UP") {
            Write-Host "`n======================================================================" -ForegroundColor Green
            Write-Host " DEPLOYMENT COMPLETED SUCCESSFULLY!" -ForegroundColor Green
            Write-Host "======================================================================" -ForegroundColor Green
            Write-Host "Application Base URL:  $baseUrl" -ForegroundColor Green
            Write-Host "Swagger UI Endpoint:   $baseUrl/swagger-ui/index.html" -ForegroundColor Green
            Write-Host "Actuator Health:       $healthUrl" -ForegroundColor Green
            Write-Host "======================================================================" -ForegroundColor Green
            exit 0
        }
    } catch {
        # Ignore web exceptions and try again
    }
    Write-Host "Waiting for app to start up and report healthy... [retry $i/20]" -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

Write-Error "Deployment succeeded but health check did not return HTTP 200/UP within the timeout window."
exit 1
