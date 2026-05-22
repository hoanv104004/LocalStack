#!/bin/bash
set -euxo pipefail

echo "=== Initializing LocalStack AWS resources ==="

create_dynamodb_table() {
    local table=$1
    local key_schema=$2
    local attr_defs=$3
    if awslocal dynamodb list-tables --query "TableNames[?@=='$table']" --output text | grep -q "$table"; then
        echo "Table '$table' already exists, skipping"
    else
        awslocal dynamodb create-table --table-name "$table" --key-schema "$key_schema" --attribute-definitions "$attr_defs" --billing-mode PAY_PER_REQUEST
    fi
}

create_sqs_queue() {
    local queue=$1
    if awslocal sqs list-queues --queue-name-prefix "$queue" --output text | grep -q "$queue"; then
        echo "Queue '$queue' already exists, skipping"
    else
        awslocal sqs create-queue --queue-name "$queue"
    fi
}

# DynamoDB Tables
create_dynamodb_table "accounts" "AttributeName=accountId,KeyType=HASH" "AttributeName=accountId,AttributeType=S"
create_dynamodb_table "transactions" "AttributeName=transactionId,KeyType=HASH" "AttributeName=transactionId,AttributeType=S"
create_dynamodb_table "statements" "AttributeName=statementId,KeyType=HASH" "AttributeName=statementId,AttributeType=S"

# SQS Queues
create_sqs_queue "transaction-notification"
create_sqs_queue "statement-generation"

# SNS Topic
awslocal sns create-topic --name transaction-events 2>/dev/null || echo "Topic 'transaction-events' already exists"

# Subscribe notification queue to SNS topic
TOPIC_ARN=$(awslocal sns list-topics --query "Topics[?contains(TopicArn, 'transaction-events')].TopicArn" --output text)
QUEUE_URL=$(awslocal sqs get-queue-url --queue-name transaction-notification --query QueueUrl --output text)
QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names QueueArn --query "Attributes.QueueArn" --output text)
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QUEUE_ARN" 2>/dev/null || echo "Subscription already exists"

# Subscribe statement queue to SNS topic
QUEUE2_URL=$(awslocal sqs get-queue-url --queue-name statement-generation --query QueueUrl --output text)
QUEUE2_ARN=$(awslocal sqs get-queue-attributes --queue-url "$QUEUE2_URL" --attribute-names QueueArn --query "Attributes.QueueArn" --output text)
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QUEUE2_ARN" 2>/dev/null || echo "Subscription already exists"

# S3 Bucket
awslocal s3 mb s3://bank-statements 2>/dev/null || echo "Bucket 'bank-statements' already exists"

# SES
awslocal ses verify-email-identity --email-address no-reply@bank.local 2>/dev/null || echo "Email already verified"

echo "=== Initialization complete ==="
