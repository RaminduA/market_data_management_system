package com.scorpion.marketdata.api.dto;

public class UpdateRequest {
    private final String correlationId;
    private final MarketDataDto marketDataDto;

    public UpdateRequest() {
        this.correlationId = null;
        this.marketDataDto = null;
    }

    public UpdateRequest(String correlationId, MarketDataDto marketDataDto) {
        this.correlationId = correlationId;
        this.marketDataDto = marketDataDto;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public MarketDataDto getMarketData() {
        return marketDataDto;
    }
}
