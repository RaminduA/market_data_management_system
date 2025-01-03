package com.scorpion.marketdata.api.broker;

import com.scorpion.marketdata.api.controller.MarketDataController;
import com.scorpion.marketdata.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataApiBroker {
    private static final Logger log = LoggerFactory.getLogger(MarketDataController.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    public MarketDataApiBroker(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

    public ResponseEntity<String> addMarketData(MarketDataDto request) {
        String correlationId = generateCorrelationId();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        log.error("Request Body: " + request.toString());

        UpdateRequest kafkaRequest = new UpdateRequest(correlationId, request);

        log.warn("Sending market data update request");

        Message<UpdateRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-update")
                .build();

        kafkaTemplate.send(message);

        try {
            String result = future.join();
            log.warn("Sending market data update response");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating market data.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<String> getMarketDataSpecific (String symbol, String source) {
        String correlationId = generateCorrelationId();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        GetSpecificRequest kafkaRequest = new GetSpecificRequest(correlationId, symbol, source);

        log.warn("Sending market data query specific request");

        Message<GetSpecificRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-query-specific")
                .build();

        kafkaTemplate.send(message);

        try {
            String result = future.join();
            log.warn("Sending market data query specific response");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching market data.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<String> getMarketDataConsolidated(String symbol) {
        String correlationId = generateCorrelationId();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        GetConsolidatedRequest kafkaRequest = new GetConsolidatedRequest(correlationId, symbol);

        log.warn("Sending market data query consolidated request");

        Message<GetConsolidatedRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-query-consolidated")
                .build();

        kafkaTemplate.send(message);

        try {
            String result = future.join();
            log.warn("Sending market data query consolidated response");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching consolidated market data.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<String> getMarketDataBatch(List<String> request) {
        String correlationId = generateCorrelationId();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        GetConsolidatedBatchRequest kafkaRequest = new GetConsolidatedBatchRequest(correlationId, request);

        log.warn("Sending market data query consolidated batch request");

        Message<GetConsolidatedBatchRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-query-consolidated-batch")
                .build();

        kafkaTemplate.send(message);

        try {
            String result = future.join();
            log.warn("Sending market data query consolidated batch response");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching consolidated market data batch.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<String> deleteMarketData(String symbol, String source) {
        String correlationId = generateCorrelationId();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        DeleteRequest kafkaRequest = new DeleteRequest(correlationId, symbol, source);

        log.warn("Sending market data delete request");

        Message<DeleteRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-delete")
                .build();

        kafkaTemplate.send(message);

        try {
            String result = future.join();
            log.warn("Sending market data delete response");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting market data.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    @KafkaListener(topics = "market-data-response", groupId = "market-data-api")
    public void handleCoreResponse(TempPair response) {
        String correlationId = response.getCorrelationId();
        String data = response.getData();

        log.warn("Received market data response");
        CompletableFuture<String> future = pendingRequests.get(correlationId);

        if (future != null) {
            log.warn("Completing future");
            future.complete(data);
        }
    }
}
