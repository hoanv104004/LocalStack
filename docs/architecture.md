# Banking Transaction System — Kiến trúc hệ thống

## Tổng quan

Hệ thống Banking Transaction gồm 5 microservices + 1 AWS Lambda, sử dụng **LocalStack** để giả lập AWS services (DynamoDB, SQS, SNS, S3, SES, Lambda) trên môi trường local.

## Service Topology

```
┌─────────────────────────────────────────────────────┐
│                    Client                            │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────┐
│                 api-gateway (:8080)                    │
│              Spring Cloud Gateway                     │
└──┬──────────┬──────────┬─────────────────────────────┘
   │          │          │
   ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌──────────┐
│account │ │transact│ │statement │
│:8081   │ │:8082   │ │:8084     │
└───┬────┘ └───┬────┘ └────┬─────┘
    │          │           │
    │     ┌────┴────┐      │
    │     │  SNS    │      │
    │     │ topic   │      │
    │     └──┬──┬───┘      │
    │        │  │          │
    ▼        ▼  ▼          ▼
┌────────┐ ┌────────┐ ┌──────────┐
│DynamoDB│ │SQS     │ │S3        │
│account │ │notif.  │ │statements│
│transact│ │stmnt.  │ └──────────┘
└────────┘ └──┬──┬──┘
              │  │
              ▼  ▼
       ┌──────────┐ ┌──────────────┐
       │notif.    │ │Lambda        │
       │:8083     │ │statement-gen │
       │(SES)     │ │(S3 upload)   │
       └──────────┘ └──────────────┘
```

## Các service

### 1. api-gateway (Port 8080)
- Spring Cloud Gateway
- Routing: `/api/accounts/**` → account-service, `/api/transactions/**` → transaction-service, `/api/statements/**` → statement-service

### 2. account-service (Port 8081)
- Quản lý tài khoản ngân hàng
- DynamoDB table: `accounts`
- Endpoints: POST /api/accounts, GET /api/accounts/{id}, GET /api/accounts/{id}/balance, PUT /api/accounts/{id}/balance

### 3. transaction-service (Port 8082)
- Xử lý giao dịch: chuyển tiền, nạp tiền, rút tiền
- DynamoDB table: `transactions`
- Publish event lên SNS topic `transaction-events` sau mỗi giao dịch
- Endpoints: POST /api/transactions/transfer, /deposit, /withdraw

### 4. notification-service (Port 8083)
- Background consumer, poll SQS queue `transaction-notification`
- Gửi email receipt qua SES
- Chạy với web-application-type=none (không có web server)

### 5. statement-service (Port 8084)
- Tạo sao kê giao dịch, upload lên S3
- DynamoDB table: `statements`
- Endpoints: POST /api/statements, GET /api/statements/{id}, GET /api/statements/{id}/download

### 6. Lambda — statement-generator (LocalStack)
- Triggered bởi SQS `statement-generation`
- Parse transaction event từ SNS, tạo file text sao kê, upload lên S3 bucket `bank-statements`

## Data Flow — Chuyển tiền

```
Client
  → POST /api/transactions/transfer (api-gateway)
    → transaction-service
      1. Kiểm tra số dư (gọi account-service GET balance)
      2. Trừ tiền người gửi (PUT balance)
      3. Cộng tiền người nhận (PUT balance)
      4. Ghi transaction vào DynamoDB
      5. Publish event lên SNS topic
      6. Trả response

SNS → transaction-notification (SQS)
  → notification-service (poll)
    → Gửi email receipt via SES

SNS → statement-generation (SQS)
  → Lambda statement-generator
    → Tạo file sao kê → upload S3
```

## AWS Resources (LocalStack)

| Service | Resource | Usage |
|---------|----------|-------|
| DynamoDB | `accounts` | Lưu thông tin tài khoản |
| DynamoDB | `transactions` | Lưu lịch sử giao dịch |
| DynamoDB | `statements` | Metadata sao kê |
| SQS | `transaction-notification` | Queue cho notification-service |
| SQS | `statement-generation` | Queue cho Lambda |
| SNS | `transaction-events` | Topic publish transaction events |
| S3 | `bank-statements` | Lưu file sao kê PDF/text |
| SES | — | Gửi email receipt (sandbox) |
| Lambda | `statement-generator` | Xử lý async tạo sao kê |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Gateway | Spring Cloud Gateway 4.1.5 |
| AWS SDK | AWS SDK v2 (DynamoDB, SQS, SNS, S3, SES) |
| Build | Gradle 8.7, multi-project |
| AWS Emulator | LocalStack (Docker) |
| Database | DynamoDB (LocalStack) |
