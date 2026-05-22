package com.bank.transaction.service;

import com.bank.transaction.domain.Transaction;
import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.dto.TransferResponse;
import com.bank.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    public TransactionService(AccountServiceClient accountServiceClient,
                              TransactionRepository transactionRepository,
                              TransactionEventPublisher eventPublisher) {
        this.accountServiceClient = accountServiceClient;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    public TransferResponse transfer(TransferRequest request) {
        BigDecimal senderBalance = accountServiceClient.getBalance(request.getFromAccountId());
        if (senderBalance.compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance in account: " + request.getFromAccountId());
        }
        BigDecimal senderNewBalance = senderBalance.subtract(request.getAmount());
        accountServiceClient.updateBalance(request.getFromAccountId(), senderNewBalance);

        BigDecimal receiverBalance = accountServiceClient.getBalance(request.getToAccountId());
        BigDecimal receiverNewBalance = receiverBalance.add(request.getAmount());
        accountServiceClient.updateBalance(request.getToAccountId(), receiverNewBalance);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setFromAccountId(request.getFromAccountId());
        transaction.setToAccountId(request.getToAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setType("TRANSFER");
        transaction.setStatus("COMPLETED");
        transaction.setTimestamp(Instant.now());
        transaction.setDescription(request.getDescription());
        transactionRepository.save(transaction);

        eventPublisher.publish(transaction);

        return toResponse(transaction);
    }

    public TransferResponse deposit(String accountId, BigDecimal amount, String description) {
        BigDecimal currentBalance = accountServiceClient.getBalance(accountId);
        BigDecimal newBalance = currentBalance.add(amount);
        accountServiceClient.updateBalance(accountId, newBalance);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setToAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setType("DEPOSIT");
        transaction.setStatus("COMPLETED");
        transaction.setTimestamp(Instant.now());
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        eventPublisher.publish(transaction);

        return toResponse(transaction);
    }

    public TransferResponse withdraw(String accountId, BigDecimal amount, String description) {
        BigDecimal currentBalance = accountServiceClient.getBalance(accountId);
        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance in account: " + accountId);
        }
        BigDecimal newBalance = currentBalance.subtract(amount);
        accountServiceClient.updateBalance(accountId, newBalance);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setFromAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setType("WITHDRAW");
        transaction.setStatus("COMPLETED");
        transaction.setTimestamp(Instant.now());
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        eventPublisher.publish(transaction);

        return toResponse(transaction);
    }

    private TransferResponse toResponse(Transaction transaction) {
        return new TransferResponse(
                transaction.getTransactionId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getTimestamp(),
                transaction.getDescription()
        );
    }
}
