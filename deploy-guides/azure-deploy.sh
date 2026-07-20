#!/usr/bin/env bash
# ==============================================================================
# Azure App Service Deployment Script for LandLens Backend
# ==============================================================================
set -euo pipefail

# Configurations
RESOURCE_GROUP="landlens-rg"
LOCATION="eastus"
ACR_NAME="landlensregistry"
APP_SERVICE_PLAN="landlens-plan"
WEB_APP_NAME="landlens-backend-app"

echo "=================================================="
echo "Starting Azure App Service Deployment..."
echo "=================================================="

# 1. Login verification
echo "Checking Azure CLI connection..."
if ! az account show >/dev/null 2>&1; then
    echo "Please run 'az login' first to authenticate with Azure."
    exit 1
fi

# 2. Create Resource Group
echo "Creating resource group ${RESOURCE_GROUP} in ${LOCATION}..."
az group create --name "$RESOURCE_GROUP" --location "$LOCATION"

# 3. Create Azure Container Registry (ACR)
echo "Creating container registry ${ACR_NAME}..."
az acr create --resource-group "$RESOURCE_GROUP" --name "$ACR_NAME" --sku Basic --admin-enabled true

# Get ACR credentials
ACR_USERNAME=$(az acr credential show --name "$ACR_NAME" --query "username" -o tsv)
ACR_PASSWORD=$(az acr credential show --name "$ACR_NAME" --query "passwords[0].value" -o tsv)
ACR_LOGIN_SERVER="${ACR_NAME}.azurecr.io"

# 4. Build and push image to ACR
echo "Logging in to registry ${ACR_LOGIN_SERVER}..."
docker login "$ACR_LOGIN_SERVER" -u "$ACR_USERNAME" -p "$ACR_PASSWORD"

echo "Building Docker image..."
docker build -t "${ACR_LOGIN_SERVER}/landlens-backend:latest" .

echo "Pushing Docker image..."
docker push "${ACR_LOGIN_SERVER}/landlens-backend:latest"

# 5. Create App Service Plan (Linux Web Apps)
echo "Creating app service plan ${APP_SERVICE_PLAN}..."
az appservice plan create --name "$APP_SERVICE_PLAN" --resource-group "$RESOURCE_GROUP" --is-linux --sku B1

# 6. Create Web App for Containers
echo "Creating web app ${WEB_APP_NAME}..."
az webapp create --resource-group "$RESOURCE_GROUP" --plan "$APP_SERVICE_PLAN" --name "$WEB_APP_NAME" \
  --deployment-container-image-name "${ACR_LOGIN_SERVER}/landlens-backend:latest"

# 7. Configure Container settings and Registry Credentials
echo "Configuring registry credentials on web app..."
az webapp config container set --name "$WEB_APP_NAME" --resource-group "$RESOURCE_GROUP" \
  --docker-custom-image-name "${ACR_LOGIN_SERVER}/landlens-backend:latest" \
  --docker-registry-server-url "https://${ACR_LOGIN_SERVER}" \
  --docker-registry-server-user "$ACR_USERNAME" \
  --docker-registry-server-password "$ACR_PASSWORD"

# 8. Configure Environment variables on Azure Web App
echo "Configuring application environment settings..."
az webapp config appsettings set --resource-group "$RESOURCE_GROUP" --name "$WEB_APP_NAME" --settings \
  SPRING_PROFILES_ACTIVE="prod" \
  WEBSITES_PORT="8080" \
  DB_URL="jdbc:mysql://srv1117.hstgr.io:3306/u833088220_ll1234?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  DB_USERNAME="u833088220_ll1234" \
  DB_PASSWORD="ll12342D" \
  JWT_SECRET="9a2f3f4e5d6c7b8a9f0e1d2c3b4a5f6e7d8c9b0a1f2e3d4c5b6a7f8e9d0c1b2a3"

# 9. Output Base URL
echo "=================================================="
echo "SUCCESS: LandLens Backend is running on Azure!"
echo "Access via: https://${WEB_APP_NAME}.azurewebsites.net/"
echo "Health Check: https://${WEB_APP_NAME}.azurewebsites.net/actuator/health"
echo "=================================================="
