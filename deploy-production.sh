#!/bin/bash

echo "🚀 SfaAI Production Deployment Script"
echo "====================================="

# Configuration
APP_NAME="sfaai"
JAR_FILE="target/SfaAI-0.0.1-SNAPSHOT.jar"
PROFILE="prod"
PORT=8080

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root"
   exit 1
fi

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    print_error "JAR file not found: $JAR_FILE"
    print_status "Building application..."
    ./mvnw clean package -DskipTests
fi

# Check if environment file exists
if [ ! -f ".env.production" ]; then
    print_warning "Production environment file not found: .env.production"
    print_status "Please create .env.production from env.production.template"
    exit 1
fi

# Load environment variables
print_status "Loading production environment variables..."
source .env.production

# Validate required environment variables
required_vars=("DATABASE_URL" "DATABASE_USERNAME" "DATABASE_PASSWORD" "JWT_SECRET" "VAPI_API_KEY" "ELEVENLABS_API_KEY")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        print_error "Required environment variable $var is not set"
        exit 1
    fi
done

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p /var/log/sfaai
mkdir -p /var/run/sfaai
mkdir -p /etc/sfaai

# Set proper permissions
print_status "Setting proper permissions..."
sudo chown -R $USER:$USER /var/log/sfaai
sudo chown -R $USER:$USER /var/run/sfaai
sudo chmod 755 /var/log/sfaai
sudo chmod 755 /var/run/sfaai

# Stop existing application if running
print_status "Stopping existing application..."
pkill -f "$JAR_FILE" || true
sleep 5

# Backup existing application
if [ -f "/var/run/sfaai/$APP_NAME.jar" ]; then
    print_status "Backing up existing application..."
    cp "/var/run/sfaai/$APP_NAME.jar" "/var/run/sfaai/$APP_NAME.jar.backup.$(date +%Y%m%d_%H%M%S)"
fi

# Copy new JAR file
print_status "Deploying new application..."
cp "$JAR_FILE" "/var/run/sfaai/$APP_NAME.jar"

# Create systemd service file
print_status "Creating systemd service..."
sudo tee /etc/systemd/system/sfaai.service > /dev/null <<EOF
[Unit]
Description=SfaAI Application
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=/var/run/sfaai
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=$PROFILE -Dserver.port=$PORT /var/run/sfaai/$APP_NAME.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=sfaai

# Environment variables
Environment="DATABASE_URL=$DATABASE_URL"
Environment="DATABASE_USERNAME=$DATABASE_USERNAME"
Environment="DATABASE_PASSWORD=$DATABASE_PASSWORD"
Environment="JWT_SECRET=$JWT_SECRET"
Environment="VAPI_API_KEY=$VAPI_API_KEY"
Environment="ELEVENLABS_API_KEY=$ELEVENLABS_API_KEY"
Environment="CORS_ALLOWED_ORIGINS=$CORS_ALLOWED_ORIGINS"
Environment="AUDIO_BASE_URL=$AUDIO_BASE_URL"

# Security settings
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ReadWritePaths=/var/log/sfaai /var/run/sfaai

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd and enable service
print_status "Enabling and starting service..."
sudo systemctl daemon-reload
sudo systemctl enable sfaai.service
sudo systemctl start sfaai.service

# Wait for application to start
print_status "Waiting for application to start..."
sleep 10

# Check if application is running
if systemctl is-active --quiet sfaai.service; then
    print_status "Application is running successfully!"
    
    # Check application health
    print_status "Checking application health..."
    if curl -f -s http://localhost:$PORT/actuator/health > /dev/null; then
        print_status "Health check passed!"
    else
        print_warning "Health check failed, but service is running"
    fi
    
    # Show service status
    print_status "Service status:"
    sudo systemctl status sfaai.service --no-pager -l
    
else
    print_error "Failed to start application"
    sudo systemctl status sfaai.service --no-pager -l
    exit 1
fi

print_status "Deployment completed successfully!"
print_status "Application is running on port $PORT"
print_status "Logs: sudo journalctl -u sfaai -f"
print_status "Status: sudo systemctl status sfaai" 