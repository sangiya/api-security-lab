package com.sangiya.apisec.transfer;

import java.math.BigDecimal;

import com.sangiya.apisec.transfer.validation.ValidAccountNumber;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TransferRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    @DecimalMax(value = "10000.00", message = "amount must not exceed 10000.00")
    private BigDecimal amount;

    @NotBlank(message = "recipient is required")
    @Size(min = 9, max = 9, message = "recipient account number must be 9 characters")
    @ValidAccountNumber
    private String recipient;

    @Size(max = 255, message = "reference must not exceed 255 characters")
    private String reference;

    public TransferRequest() {
    }

    public TransferRequest(BigDecimal amount, String recipient, String reference) {
        this.amount = amount;
        this.recipient = recipient;
        this.reference = reference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
