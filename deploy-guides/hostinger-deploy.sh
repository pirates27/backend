#!/usr/bin/env bash
# ==============================================================================
# Hostinger VPS Deploy Script for LandLens Backend
# ==============================================================================
set -euo pipefail

echo "=================================================="
echo "Starting Hostinger VPS Deployment for LandLens..."
echo "=================================================="

# 1. Update OS package indexes
echo "Updating packages..."
sudo apt-get update -y && sudo apt-get upgrade -y

# 2. Check and install Docker + Docker Compose if missing
if ! [ -x "$(command -v docker)" ]; then
    echo "Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
fi

if ! [ -x "$(command -v docker-compose)" ]; then
    echo "Installing Docker Compose..."
    sudo apt-get install -y docker-compose
fi

# 3. Create app deployment directory
APP_DIR="/opt/landlens-backend"
echo "Creating application directory: ${APP_DIR}..."
sudo mkdir -p "${APP_DIR}"
sudo chown -R $USER:$USER "${APP_DIR}"

# 4. Copy application files (Run this script inside the project folder copied to VPS)
echo "Copying files to application directory..."
cp -R ./* "${APP_DIR}/"
cd "${APP_DIR}"

# 5. Define environment variables in local env file
ENV_FILE=".env"
if [ ! -f "$ENV_FILE" ]; then
    echo "Creating environment file: ${ENV_FILE}..."
    cat <<EOF > "$ENV_FILE"
DB_URL=jdbc:mysql://mysql:3306/u833088220_ll1234?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useInformationSchema=true
DB_USERNAME=u833088220_ll1234
DB_PASSWORD=ll12342D
JWT_SECRET=9a2f3f4e5d6c7b8a9f0e1d2c3b4a5f6e7d8c9b0a1f2e3d4c5b6a7f8e9d0c1b2a3
SPRING_PROFILES_ACTIVE=prod
EOF
    echo "Environment variables created in .env. Please update credentials if necessary."
fi

# 6. Stop existing containers if running
echo "Stopping any existing containers..."
docker-compose down || true

# 7. Start containerized application with volume persistence
echo "Building and starting LandLens containers..."
docker-compose up -d --build

# 8. Check health
echo "Waiting for app to start and respond to health check..."
sleep 15
for i in {1..12}; do
    if curl -s -f http://localhost:8080/actuator/health | grep -q "UP"; then
        echo "=================================================="
        echo "SUCCESS: LandLens Backend is running on Hostinger!"
        echo "Access via: http://<your-vps-ip>:8080/"
        echo "Health Check: http://<your-vps-ip>:8080/actuator/health"
        echo "=================================================="
        exit 0
    fi
    echo "Waiting for app to be healthy (retry $i/12)..."
    sleep 5
done

echo "WARNING: Containers started, but healthcheck did not report UP yet. Please run 'docker logs landlens-backend-app'."
