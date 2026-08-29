package com.sangiya.apisec.transfer;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

/**
 * Records validated transfer requests. Pure in-memory ledger for the lab;
 * a production implementation would write to an idempotent ledger service
 * and enforce double-spend/balance checks.
 */
@Service
public class TransferService {

    private final ConcurrentMap<String, TransferResult> transfers = new ConcurrentHashMap<>();

    public TransferResult execute(String fromUsername, TransferRequest request) {
        TransferResult result = new TransferResult(
                UUID.randomUUID().toString(),
                fromUsername,
                request.getRecipient(),
                request.getAmount(),
                Instant.now());
        transfers.put(result.getId(), result);
        return result;
    }

    public int totalTransfers() {
        return transfers.size();
    }
}
