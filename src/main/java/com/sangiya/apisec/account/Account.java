package com.sangiya.apisec.account;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {

    private final String accountNumber;
    private final String owner;
    private final BigDecimal balance;
    private final String currency;

    public Account(String accountNumber, String owner, BigDecimal balance, String currency) {
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.owner = Objects.requireNonNull(owner);
        this.balance = Objects.requireNonNull(balance);
        this.currency = Objects.requireNonNull(currency);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}
