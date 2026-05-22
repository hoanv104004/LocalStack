# Quickstart Guide

## Prerequisites

- **Java 21** — kiểm tra: `java -version`
- **Docker** — kiểm tra: `docker --version`
- **Gradle 8.7+** — wrapper tự động tải nếu cần
- **AWS CLI** (tùy chọn) — để debug LocalStack: `aws --endpoint-url=http://localhost:4566`

## 1. Khởi động LocalStack

```bash
cd localstack
docker compose up -d
```

Chờ init script chạy (~10-15 giây). Kiểm tra resources:

```bash
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
aws --endpoint-url=http://localhost:4566 sqs list-queues
aws --endpoint-url=http://localhost:4566 s3 ls
```

Expected output: `accounts`, `transactions`, `statements` tables; `transaction-notification`, `statement-generation` queues; `bank-statements` bucket.

## 2. Build toàn bộ project

```bash
cd banking-platform
.\gradlew build
```

## 3. Chạy các service (mỗi service một terminal riêng)

```bash
# Terminal 1: account-service
.\gradlew :account-service:bootRun

# Terminal 2: transaction-service
.\gradlew :transaction-service:bootRun

# Terminal 3: notification-service
.\gradlew :notification-service:bootRun

# Terminal 4: statement-service
.\gradlew :statement-service:bootRun

# Terminal 5: api-gateway
.\gradlew :api-gateway:bootRun
```

Hoặc chạy background (PowerShell):
```powershell
Start-Process -WindowStyle Hidden -FilePath "powershell" -ArgumentList ".\gradlew :account-service:bootRun"
Start-Process -WindowStyle Hidden -FilePath "powershell" -ArgumentList ".\gradlew :transaction-service:bootRun"
```

## 4. Test API

### Tạo tài khoản

```powershell
# Tạo tài khoản Alice
Invoke-RestMethod -Uri "http://localhost:8080/api/accounts" -Method Post `
  -Body '{"accountId":"ACC001","ownerName":"Alice"}' `
  -ContentType "application/json"

# Tạo tài khoản Bob
Invoke-RestMethod -Uri "http://localhost:8080/api/accounts" -Method Post `
  -Body '{"accountId":"ACC002","ownerName":"Bob"}' `
  -ContentType "application/json"
```

### Nạp tiền

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions/deposit" -Method Post `
  -Body '{"accountId":"ACC001","amount":1000,"description":"Initial deposit"}' `
  -ContentType "application/json"
```

### Chuyển tiền

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions/transfer" -Method Post `
  -Body '{"fromAccountId":"ACC001","toAccountId":"ACC002","amount":200,"description":"Pizza split"}' `
  -ContentType "application/json"
```

### Kiểm tra số dư

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/accounts/ACC001/balance"
Invoke-RestMethod -Uri "http://localhost:8080/api/accounts/ACC002/balance"
```

### Tạo sao kê

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/statements" -Method Post `
  -Body '{"accountId":"ACC001","content":"Statement for ACC001"}' `
  -ContentType "application/json"
```

## 5. Deploy Lambda (LocalStack)

```powershell
# Build Lambda zip
.\gradlew :lambda-statement:buildZip

# Tạo function
aws --endpoint-url=http://localhost:4566 lambda create-function `
  --function-name statement-generator `
  --runtime java21 `
  --role arn:aws:iam::000000000000:role/lambda-role `
  --handler com.bank.lambda.StatementGeneratorHandler `
  --zip-file fileb://lambda-statement/build/distributions/lambda-statement-0.0.1-SNAPSHOT.zip `
  --timeout 30

# Subscribe Lambda to SQS
$queueUrl = aws --endpoint-url=http://localhost:4566 sqs get-queue-url --queue-name statement-generation --query QueueUrl --output text
$queueArn = aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes --queue-url $queueUrl --attribute-names QueueArn --query "Attributes.QueueArn" --output text
aws --endpoint-url=http://localhost:4566 lambda create-event-source-mapping `
  --function-name statement-generator `
  --event-source-arn $queueArn
```

## 6. Dừng LocalStack

```bash
cd localstack
docker compose down -v   # -v để xóa volume (xóa dữ liệu)
```

## Cấu trúc thư mục

```
banking-platform/
├── localstack/              # Docker Compose + init script
├── api-gateway/             # Spring Cloud Gateway
├── account-service/         # Account management
├── transaction-service/     # Transaction processing
├── notification-service/    # SQS consumer + SES email
├── statement-service/       # S3 storage + DynamoDB metadata
├── lambda-statement/        # AWS Lambda (LocalStack)
├── docs/                    # Tài liệu
└── build.gradle             # Root Gradle config
```

## Troubleshooting

| Vấn đề | Cách fix |
|--------|----------|
| `Connection refused` khi gọi API | Kiểm tra service đã chạy chưa, kiểm tra port |
| DynamoDB table not found | Chạy `docker compose down -v && docker compose up -d` để re-init |
| Lambda không chạy | Deploy lại Lambda với `--timeout 30` |
| SQS queue empty | Kiểm tra SNS subscription, thực hiện giao dịch trước |
