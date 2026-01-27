#!/bin/bash
set -e

# 1. Build Frontend
echo "----------------------------------------------------------------"
echo "Step 1: Building Frontend (itasset-front)"
echo "----------------------------------------------------------------"
cd itasset-front
# Load nvm source
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

nvm use 22
npm install -g pnpm
pnpm install
pnpm build
cd ..

# 2. Build Backend
echo "----------------------------------------------------------------"
echo "Step 2: Building Backend (ciyo-admin)"
echo "----------------------------------------------------------------"
# Hide sensitive configuration file to prevent it from being packaged
SENSITIVE_FILE="ciyo-admin/src/main/resources/application-prod1.yml"
TEMP_BACKUP="application-prod1.yml.tmp"

if [ -f "$SENSITIVE_FILE" ]; then
  # Set up trap to ensure file is restored even if build fails or script exits
  trap 'echo "Restoring $SENSITIVE_FILE..."; [ -f "$TEMP_BACKUP" ] && mv "$TEMP_BACKUP" "$SENSITIVE_FILE"' EXIT

  echo "Temporarily moving $SENSITIVE_FILE to avoid packaging secrets..."
  mv "$SENSITIVE_FILE" "$TEMP_BACKUP"
fi

# The maven-resources-plugin configured in ciyo-admin/pom.xml will automatically
# copy the ../itasset-front/dist folder to target/classes/static
mvn clean package -DskipTests


echo "----------------------------------------------------------------"
echo "Build Complete!"
echo "JAR file: ciyo-admin/target/ciyo-admin.jar"
echo "----------------------------------------------------------------"

# 3. Upload to Server (Optional)
# To use: SERVER_HOST=x.x.x.x ./package.sh or uncomment/edit variables below
SERVER_USER=${SERVER_USER:-"root"}
SERVER_HOST=${SERVER_HOST:-"101.35.25.237"}
TARGET_DIR=${TARGET_DIR:-"/usr/share/nginx/html"}

if [ -n "$SERVER_HOST" ]; then
  echo "----------------------------------------------------------------"
  echo "Step 3: Uploading JAR to $SERVER_HOST"
  echo "----------------------------------------------------------------"
  # Note: Ensure you have SSH key access or you will be prompted for password
  scp ciyo-admin/target/ciyo-admin.jar ${SERVER_USER}@${SERVER_HOST}:${TARGET_DIR}
  
  # Optional: Restart service on server
  # ssh ${SERVER_USER}@${SERVER_HOST} "systemctl restart ciyo-admin"
  
  echo "Upload complete!"
  echo "----------------------------------------------------------------"
else
  echo "Skip Upload: SERVER_HOST is not set."
  echo "Usage example: SERVER_HOST=192.168.1.100 ./package.sh"
  echo "----------------------------------------------------------------"
fi
