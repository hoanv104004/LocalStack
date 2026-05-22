package com.bank.account.service;

import com.bank.account.domain.Account;
import com.bank.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String accountId, String ownerName) {
        if (accountRepository.findById(accountId) != null) {
            throw new IllegalArgumentException("Account already exists: " + accountId);
        }
        Account account = new Account();
        account.setAccountId(accountId);
        account.setOwnerName(ownerName);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        accountRepository.save(account);
        return account;
    }

    public Account getAccount(String accountId) {
        return accountRepository.findById(accountId);
    }

    public BigDecimal getBalance(String accountId) {
        Account account = accountRepository.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        return account.getBalance();
    }

    public Account updateBalance(String accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        account.setBalance(newBalance);
        accountRepository.save(account);
        return account;
    }
}
