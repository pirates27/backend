# ==============================================================================
# LandLens Windows PowerShell ECS Fargate Deployment Automation Script (CodeBuild)
# ==============================================================================
# Add current workspace directory to PATH so local terraform.exe is found
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
$containerName = "landlens-backend-container"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Starting LandLens Production Deployment to AWS ECS Fargate via CodeBuild (Windows)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Dependency Checks
Write-Host "[1/9] Checking tool dependencies..." -ForegroundColor Yellow
$dependencies = @("aws", "terraform")
foreach ($dep in $dependencies) {
    if (-not (Get-Command $dep -ErrorAction SilentlyContinue)) {
        Write-Error "Required tool '$dep' is not installed or not in your system environment PATH. Please install it to run this script."
        exit 1
    }
}

# 2. Provision AWS Infrastructure via Terraform (First Pass)
Write-Host "[2/9] Initializing and applying Terraform templates (First Pass)..." -ForegroundColor Yellow
Push-Location terraform
terraform init
terraform apply -auto-approve

# Extract outputs
$codebuildProject = (terraform output -raw codebuild_project_name).Trim()
$bucketName = (terraform output -raw codebuild_bucket_name).Trim()
$albDnsName = (terraform output -raw alb_dns_name).Trim()
$ecsCluster = (terraform output -raw ecs_cluster_name).Trim()
$ecsService = (terraform output -raw ecs_service_name).Trim()
Pop-Location

Write-Host "Infrastructure resources verified." -ForegroundColor Green

# 3. Zip Application Source Code
Write-Host "[3/9] Compressing project source code..." -ForegroundColor Yellow
if (Test-Path "source.zip") {
    Remove-Item "source.zip" -Force
}
& tar -a -c -f source.zip --exclude=target --exclude=.git --exclude=.terraform --exclude=source.zip --exclude=terraform.exe --exclude=terraform .
Write-Host "Source zipped successfully: source.zip" -ForegroundColor Green

# 4. Upload Source to S3
Write-Host "[4/9] Uploading source bundle to S3 bucket ($bucketName)..." -ForegroundColor Yellow
aws s3 cp source.zip "s3://$bucketName/source.zip" --region $awsRegion

# 5. Trigger AWS CodeBuild Job
Write-Host "[5/9] Triggering AWS CodeBuild build..." -ForegroundColor Yellow
$buildResult = aws codebuild start-build --project-name $codebuildProject --region $awsRegion | ConvertFrom-Json
$buildId = $buildResult.build.id

Write-Host "Build started with ID: $buildId" -ForegroundColor Green
Write-Host "Polling build status (this may take 2-4 minutes)..." -ForegroundColor Yellow

$buildSuccess = $false
for ($i = 1; $i -le 60; $i++) {
    Start-Sleep -Seconds 10
    $statusCheck = aws codebuild batch-get-builds --ids $buildId --region $awsRegion | ConvertFrom-Json
    $buildStatus = $statusCheck.builds[0].buildStatus
    
    if ($buildStatus -eq "SUCCEEDED") {
        Write-Host "`nAWS CodeBuild build completed successfully!" -ForegroundColor Green
        $buildSuccess = $true
        break
    } elseif ($buildStatus -eq "FAILED" -or $buildStatus -eq "FAULT" -or $buildStatus -eq "STOPPED") {
        Write-Error "`nAWS CodeBuild build failed with status: $buildStatus"
        exit 1
    }
    
    Write-Host -NoNewline "."
}

if (-not $buildSuccess) {
    Write-Error "`nAWS CodeBuild build timed out."
    exit 1
}

# Clean up zip
if (Test-Path "source.zip") {
    Remove-Item "source.zip" -Force
}

# 6. Apply Terraform Infrastructure (Second Pass to ensure everything links properly)
Write-Host "[6/9] Applying Terraform templates (Second Pass)..." -ForegroundColor Yellow
Push-Location terraform
terraform apply -auto-approve
Pop-Location

# 7. Force ECS Deployment
Write-Host "[7/9] Triggering ECS service redeployment..." -ForegroundColor Yellow
aws ecs update-service --cluster $ecsCluster --service $ecsService --force-new-deployment --region $awsRegion | Out-Null

# 8. Wait for ECS Fargate Deployment
Write-Host "[8/9] Waiting for ECS Fargate deployment to stabilize (this may take a few minutes)..." -ForegroundColor Yellow
aws ecs wait services-stable --cluster $ecsCluster --services $ecsService --region $awsRegion
Write-Host "ECS Service is stable and running." -ForegroundColor Green

# 9. Verify Health Check
Write-Host "[9/9] Verifying Actuator Health Check..." -ForegroundColor Yellow
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
