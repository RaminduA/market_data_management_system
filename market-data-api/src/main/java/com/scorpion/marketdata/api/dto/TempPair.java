package com.scorpion.marketdata.api.dto;

public class TempPair {
    private final String correlationId;
    private final String data;

    public TempPair() {
        this.correlationId = null;
        this.data = null;
    }

    public TempPair(String correlationId, String data) {
        this.correlationId = correlationId;
        this.data = data;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getData() {
        return data;
    }
}
