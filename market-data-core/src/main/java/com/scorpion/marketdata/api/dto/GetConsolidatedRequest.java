package com.scorpion.marketdata.api.dto;

public class GetConsolidatedRequest {
    private final String correlationId;
    private final String symbol;

    public GetConsolidatedRequest() {
        this.correlationId = null;
        this.symbol = null;
    }

    public GetConsolidatedRequest(String correlationId, String symbol) {
        this.correlationId = correlationId;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
