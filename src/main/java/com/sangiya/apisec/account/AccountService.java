package com.sangiya.apisec.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Read-only in-memory account registry, seeded per user. In production this
 * would come from a ledger service; kept local so the protected endpoint has
 * real payloads to serve after JWT authentication.
 */
@Service
public class AccountService {

    private final Map<String, List<Account>> accountsByOwner = new ConcurrentHashMap<>();

    public AccountService() {
        accountsByOwner.put("user", List.of(
                new Account("USR-1001", "user", new BigDecimal("1250.75"), "USD"),
                new Account("USR-1002", "user", new BigDecimal("3200.00"), "USD")));
        accountsByOwner.put("admin", List.of(
                new Account("ADM-9001", "admin", new BigDecimal("50000.00"), "USD"),
                new Account("ADM-9002", "admin", new BigDecimal("8750.40"), "EUR")));
    }

    public List<Account> accountsFor(String username) {
        return accountsByOwner.getOrDefault(username, List.of());
    }
}
