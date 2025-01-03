package com.scorpion.marketdata.api.dto;

import java.util.List;

public class GetConsolidatedBatchRequest {
    private final String correlationId;
    private final List<String> symbol;

    public GetConsolidatedBatchRequest() {
        this.correlationId = null;
        this.symbol = null;
    }

    public GetConsolidatedBatchRequest(String correlationId, List<String> symbol) {
        this.correlationId = correlationId;
        this.symbol = symbol;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public List<String> getSymbol() {
        return symbol;
    }
}
