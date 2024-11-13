#!/bin/bash

# 設定 shell 行為
set -e  # 任何命令失敗就退出
set -u  # 使用未定義的變數就退出

# 根據部署組名稱設定環境
if [[ $DEPLOYMENT_GROUP_NAME == *"test"* ]]; then
    ENVIRONMENT="test"
else
    ENVIRONMENT="prod"
fi

echo "Starting application in ${ENVIRONMENT} environment..."


# 設定應用路徑
APP_HOME="/home/ec2-user/adl-apps-nmns"

# 檢查 JAR 檔案
echo "Checking application files..."
if [ ! -f "${APP_HOME}/adl-apps-nmns-0.0.1-SNAPSHOT.jar" ]; then
    echo "JAR file not found at ${APP_HOME}"
    ls -l ${APP_HOME}
    exit 1
fi

# AWS 認證
echo "Retrieving AWS credentials..."
export AWS_ACCESS_KEY=$(aws ssm get-parameter --name "/${ENVIRONMENT}/aws/access_key_id" --with-decryption --query "Parameter.Value" --output text)
export AWS_SECRET_KEY=$(aws ssm get-parameter --name "/${ENVIRONMENT}/aws/secret_access_key" --with-decryption --query "Parameter.Value" --output text)

# 數據庫配置
echo "Retrieving database configurations..."
export DB_URL=$(aws ssm get-parameter --name "/${ENVIRONMENT}/db/url" --with-decryption --query "Parameter.Value" --output text)
export DB_USER=$(aws ssm get-parameter --name "/${ENVIRONMENT}/db/user" --with-decryption --query "Parameter.Value" --output text)
export DB_PWD=$(aws ssm get-parameter --name "/${ENVIRONMENT}/db/password" --with-decryption --query "Parameter.Value" --output text)

# 設置 Google Cloud 認證
export GOOGLE_APPLICATION_CREDENTIALS="/home/ec2-user/application_default_credentials.json"

# 直接啟動應用以查看錯誤
echo "Starting application..."
nohup java -Dspring.profiles.active=${ENVIRONMENT} \
     -jar ${APP_HOME}/adl-apps-nmns-0.0.1-SNAPSHOT.jar </dev/null >/dev/null 2>&1 &

