package com.scorpion.marketdata.core.dto;

public class UpdateResponse extends KafkaResponse {
    public UpdateResponse() {
        super();
    }

    public UpdateResponse(String correlationId, String data) {
        super(correlationId, data);
    }
}
