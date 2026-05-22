package com.bank.transaction.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AccountServiceClient {

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                                @Value("${account.service.url:http://localhost:8081}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public BigDecimal getBalance(String accountId) {
        Map<String, Object> response = restTemplate.getForObject(
                accountServiceUrl + "/api/accounts/{accountId}/balance",
                Map.class,
                accountId);
        if (response == null || response.get("balance") == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        return new BigDecimal(response.get("balance").toString());
    }

    public void updateBalance(String accountId, BigDecimal newBalance) {
        restTemplate.put(
                accountServiceUrl + "/api/accounts/{accountId}/balance",
                Map.of("balance", newBalance),
                accountId);
    }
}
