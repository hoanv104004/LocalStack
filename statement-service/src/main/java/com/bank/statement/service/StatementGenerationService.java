package com.bank.statement.service;

import com.bank.statement.domain.Statement;
import com.bank.statement.repository.StatementRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class StatementGenerationService {

    private final S3StorageService s3StorageService;
    private final StatementRepository statementRepository;

    public StatementGenerationService(S3StorageService s3StorageService,
                                      StatementRepository statementRepository) {
        this.s3StorageService = s3StorageService;
        this.statementRepository = statementRepository;
    }

    public Statement generateStatement(String accountId, String content) {
        String statementId = UUID.randomUUID().toString();
        String s3Key = "statements/" + accountId + "/" + statementId + ".txt";

        s3StorageService.upload(s3Key, content.getBytes());

        Statement statement = new Statement();
        statement.setStatementId(statementId);
        statement.setAccountId(accountId);
        statement.setS3Key(s3Key);
        statement.setCreatedAt(Instant.now());
        statementRepository.save(statement);
        return statement;
    }

    public Statement getStatement(String statementId) {
        return statementRepository.findById(statementId);
    }

    public byte[] downloadStatement(String s3Key) {
        return s3StorageService.download(s3Key);
    }
}
