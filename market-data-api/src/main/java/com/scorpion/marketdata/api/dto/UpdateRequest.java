package com.scorpion.marketdata.api.dto;

public class UpdateRequest {
    private final String correlationId;
    private final MarketDataRequestBody marketData;

    public UpdateRequest() {
        this.correlationId = null;
        this.marketData = null;
    }

    public UpdateRequest(String correlationId, MarketDataRequestBody marketData) {
        this.correlationId = correlationId;
        this.marketData = marketData;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public MarketDataRequestBody getMarketData() {
        return marketData;
    }
}
