package com.scorpion.marketdata.api.dto;

public class DeleteRequest {
    private final String correlationId;
    private final String symbol;
    private final String source;

    public DeleteRequest() {
        this.correlationId = null;
        this.symbol = null;
        this.source = null;
    }

    public DeleteRequest(String correlationId, String symbol, String source) {
        this.correlationId = correlationId;
        this.symbol = symbol;
        this.source = source;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSource() {
        return source;
    }
}
