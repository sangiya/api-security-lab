package com.sangiya.apisec.transfer;

import java.time.Instant;
import java.util.Objects;

public class TransferResult {

    private final String id;
    private final String from;
    private final String recipient;
    private final java.math.BigDecimal amount;
    private final Instant at;

    public TransferResult(String id, String from, String recipient,
                          java.math.BigDecimal amount, Instant at) {
        this.id = Objects.requireNonNull(id);
        this.from = from;
        this.recipient = recipient;
        this.amount = amount;
        this.at = at;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getRecipient() {
        return recipient;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public Instant getAt() {
        return at;
    }
}
