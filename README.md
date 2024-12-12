# 教育大數據

## 專案說明
這個專案用於從MySQL數據庫和AWS S3獲取數據，進行處理後上傳至Google Cloud Storage。

## 技術需求

- Java JDK 17
- Maven 3.x
- MySQL
- AWS S3
- Google Cloud Storage

## 環境設置

### 本地開發環境配置

#### 1. Google Cloud認證設置

##### 安裝和初始化
1. **安裝Google Cloud SDK**
   - 前往 [Google Cloud SDK下載頁面](https://cloud.google.com/sdk/docs/install)
   - 根據您的操作系統下載並安裝SDK

2. **初始化和認證**
```bash
# 初始化 Google Cloud SDK
gcloud init

# 登入Google賬戶
gcloud auth login

# 設置項目
gcloud config set project [YOUR_PROJECT_ID]
```

3. **下載認證文件**
```bash
# 創建新的應用程序默認認證
gcloud auth application-default login
```
- 認證文件默認位置：
  - Windows: `%APPDATA%/gcloud/application_default_credentials.json`
  - Linux/MacOS: `~/.config/gcloud/application_default_credentials.json`


#### 2. 配置文件設置

1. **複製配置文件模板**
   - 複製 `application.properties.sample` 到 `application-dev.properties`

2. **填寫配置信息**
```properties
# AWS
aws.access.key=${AWS_ACCESS_KEY}
aws.secret.key=${AWS_SECRET_KEY}

#S3設定
aws.s3.read.bucket.name=你的讀取桶名稱
aws.s3.read.folder=讀取文件夾路徑
aws.s3.write.bucket.name=寫入桶名稱
aws.s3.static.data.write.folder=靜態資源備份路徑
aws.s3.zip.write.folder=zip備份路徑

# GCS配置
gcs.bucket.name=GCS桶名稱
gcs.destination.folder=目標文件夾路徑

# 數據庫配置
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PWD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 測試運行

在 `test/java/com.arplanet.adlappnmns/NmnsTest.java` 中修改測試日期：

```java
@Test
void test() {
    String date = "2024-10-22";  // 修改此處日期進行測試
    List<String> dateList = new ArrayList<>();
    dateList.add(date);
    
    scheduledTaskService.processDates(dateList);
}
```

## 部署說明

### EC2環境配置

#### 1. 安裝Java 17
```bash
sudo yum install java-17-amazon-corretto
```

#### 2. 上傳Google Cloud認證配置

```bash
# 在本地執行，將認證文件上傳到EC2
scp -i [EC2密鑰文件路徑] [本地認證文件路徑] ec2-user@[EC2實例地址]:/home/ec2-user/credentials/
```

#### 3. 配置環境變量
```bash
# 編輯 .bashrc
nano ~/.bashrc

# 添加以下內容
export API_KEY=API密鑰
export GOOGLE_APPLICATION_CREDENTIALS="/home/ec2-user/credentials/application_default_credentials.json"
export AWS_ACCESS_KEY=AWS公鑰
export AWS_SECRET_KEY=AWS私鑰
export DB_URL=資料庫URL
export DB_USER=資料庫用戶名
export DB_PWD=資料庫密碼

# 重新加載配置
source ~/.bashrc

# 驗證設置
echo $GOOGLE_APPLICATION_CREDENTIALS
```

### 構建與部署
1. **application.properties設定port號**
```properties
server.port=8080
```
2. **依環境修改application-{env}.properties**
```properties
spring.jpa.open-in-view=false

aws.s3.read.bucket.name=jeremy.test
aws.s3.read.folder=raw/
aws.s3.write.bucket.name=jeremy.test
aws.s3.write.folder=edu_raw/
gcs.bucket.name=adl-apps-nmns-test
gcs.destination.folder=
```
3. **構建專案**
```bash
mvn clean package
```

4. **上傳到EC2**
```bash
scp -i [密鑰文件路徑] [本地jar包路徑] ec2-user@[EC2實例地址]:/home/ec2-user/
```

5. **運行應用**
```bash
# 測試環境運行
nohup java -jar /home/ec2-user/adl-apps-nmns-0.0.1-SNAPSHOT.jar --spring.profiles.active=test > /dev/null 2>&1 &

# 生產環境運行
nohup java -jar /home/ec2-user/adl-apps-nmns-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > /dev/null 2>&1 &

# 運行狀態查看
ps -ef | grep java
```

## 依日期手動觸發任務

如果排程執行失敗，可以通過以下API手動觸發任務：

### API端點
```
POST http://[EC2-HOST]:8080/api/task/execute/dates
Content-Type: application/json
```
### 請求範例

1. **單個日期**
```bash
curl -X POST \
-H "Content-Type: application/json" \
-H "X-API-Key: abc123" \
-d '["2024-10-22"]' \
http://ec2-54-255-172-173.ap-southeast-1.compute.amazonaws.com:8080/api/task/execute/dates
```
2. **多個日期**
```bash
curl -X POST \
-H "Content-Type: application/json" \
-H "X-API-Key: abc123" \
-d '["2024-10-22","2024-10-23","2024-10-24"]' \
http://ec2-54-255-172-173.ap-southeast-1.compute.amazonaws.com:8080/api/task/execute/dates
```

## 首次部署手動觸發任務

首次部署，可以通過以下API手動觸發日期範圍任務：

### API端點
```
POST http://[EC2-HOST]:8080/api/task/execute/range
Content-Type: application/json
```
### 請求範例

輸入起迄日(不包含當天)

```bash
curl -X POST \
-H "Content-Type: application/json" \
-H "X-API-Key: abc123" \
-d '{"startDate": "2024-10-22", "endDate": "2024-10-23"}' \
http://ec2-54-255-172-173.ap-southeast-1.compute.amazonaws.com:8080/api/task/execute/range
```

