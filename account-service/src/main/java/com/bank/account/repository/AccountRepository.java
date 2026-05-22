package com.bank.account.repository;

import com.bank.account.domain.Account;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class AccountRepository {

    private final DynamoDbTable<Account> accountTable;

    public AccountRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.accountTable = dynamoDbEnhancedClient.table("accounts",
                TableSchema.fromBean(Account.class));
    }

    public void save(Account account) {
        accountTable.putItem(account);
    }

    public Account findById(String accountId) {
        return accountTable.getItem(Key.builder().partitionValue(accountId).build());
    }

    public void deleteById(String accountId) {
        accountTable.deleteItem(Key.builder().partitionValue(accountId).build());
    }
}
