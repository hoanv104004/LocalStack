package com.bank.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferResponse {

    private final String transactionId;
    private final String fromAccountId;
    private final String toAccountId;
    private final BigDecimal amount;
    private final String status;
    private final Instant timestamp;
    private final String description;

    public TransferResponse(String transactionId, String fromAccountId, String toAccountId,
                            BigDecimal amount, String status, Instant timestamp, String description) {
        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}
