package com.scorpion.marketdata.api.dto;

public class GetSpecificRequest {
    private final String correlationId;
    private final String symbol;
    private final String source;

    public GetSpecificRequest() {
        this.correlationId = null;
        this.symbol = null;
        this.source = null;
    }

    public GetSpecificRequest(String correlationId, String symbol, String source) {
        this.correlationId = correlationId;
        this.symbol = symbol;
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
