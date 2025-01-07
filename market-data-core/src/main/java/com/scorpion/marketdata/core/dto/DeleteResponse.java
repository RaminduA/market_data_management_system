package com.scorpion.marketdata.core.dto;

public class DeleteResponse extends KafkaResponse {
    public DeleteResponse() {
        super();
    }

    public DeleteResponse(String correlationId, String data) {
        super(correlationId, data);
    }
}
