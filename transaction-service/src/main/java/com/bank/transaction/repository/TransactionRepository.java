package com.bank.transaction.repository;

import com.bank.transaction.domain.Transaction;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class TransactionRepository {

    private final DynamoDbTable<Transaction> transactionTable;

    public TransactionRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.transactionTable = dynamoDbEnhancedClient.table("transactions",
                TableSchema.fromBean(Transaction.class));
    }

    public void save(Transaction transaction) {
        transactionTable.putItem(transaction);
    }

    public Transaction findById(String transactionId) {
        return transactionTable.getItem(Key.builder().partitionValue(transactionId).build());
    }
}
