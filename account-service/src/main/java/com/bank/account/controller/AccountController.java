package com.bank.account.controller;

import com.bank.account.domain.Account;
import com.bank.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, String> request) {
        String accountId = request.get("accountId");
        String ownerName = request.get("ownerName");
        if (accountId == null || ownerName == null || accountId.isBlank() || ownerName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "accountId and ownerName are required");
        }
        Account account = accountService.createAccount(accountId, ownerName);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        Account account = accountService.getAccount(accountId);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String accountId) {
        try {
            BigDecimal balance = accountService.getBalance(accountId);
            return ResponseEntity.ok(Map.of(
                    "accountId", accountId,
                    "balance", balance
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{accountId}/balance")
    public ResponseEntity<Void> updateBalance(@PathVariable String accountId,
                                               @RequestBody Map<String, BigDecimal> request) {
        if (request.get("balance") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance is required");
        }
        try {
            accountService.updateBalance(accountId, request.get("balance"));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
