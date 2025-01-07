package com.scorpion.marketdata.core.dto;

public class KafkaResponse {
    private String correlationId;
    private Object data;

    public KafkaResponse() {
        this.correlationId = null;
        this.data = null;
    }

    public KafkaResponse(String correlationId, Object data) {
        this.correlationId = correlationId;
        this.data = data;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
