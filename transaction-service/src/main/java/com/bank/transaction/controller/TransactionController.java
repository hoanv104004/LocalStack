package com.bank.transaction.controller;

import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.dto.TransferResponse;
import com.bank.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request) {
        try {
            TransferResponse response = transactionService.transfer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransferResponse> deposit(@RequestBody Map<String, Object> request) {
        try {
            String accountId = (String) request.get("accountId");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = (String) request.get("description");
            TransferResponse response = transactionService.deposit(accountId, amount, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransferResponse> withdraw(@RequestBody Map<String, Object> request) {
        try {
            String accountId = (String) request.get("accountId");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = (String) request.get("description");
            TransferResponse response = transactionService.withdraw(accountId, amount, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
