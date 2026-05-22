package com.bank.statement.repository;

import com.bank.statement.domain.Statement;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class StatementRepository {

    private final DynamoDbTable<Statement> statementTable;

    public StatementRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.statementTable = dynamoDbEnhancedClient.table("statements",
                TableSchema.fromBean(Statement.class));
    }

    public void save(Statement statement) {
        statementTable.putItem(statement);
    }

    public Statement findById(String statementId) {
        return statementTable.getItem(Key.builder().partitionValue(statementId).build());
    }
}
