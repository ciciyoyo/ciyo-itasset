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
