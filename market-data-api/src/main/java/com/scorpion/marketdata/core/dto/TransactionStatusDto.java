package com.scorpion.marketdata.core.dto;

public class TransactionStatusDto {
    private final boolean status;
    private final String message;

    public TransactionStatusDto() {
        this.status = false;
        this.message = null;
    }

    public TransactionStatusDto(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
