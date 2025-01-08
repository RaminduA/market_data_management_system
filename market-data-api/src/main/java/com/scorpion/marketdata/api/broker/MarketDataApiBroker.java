package com.scorpion.marketdata.api.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scorpion.marketdata.api.dto.*;
import com.scorpion.marketdata.core.dto.KafkaResponse;
import com.scorpion.marketdata.core.dto.TransactionStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    private static final Logger log = LoggerFactory.getLogger(MarketDataApiBroker.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();

    public MarketDataApiBroker(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

    public ResponseEntity<Object> saveMarketData(MarketDataRequestBody request) {
        String correlationId = generateCorrelationId();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        log.error("Request Body: {}", request.toString());

        UpdateRequest kafkaRequest = new UpdateRequest(correlationId, request);

        log.warn("Sending market data update request");

        Message<UpdateRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-update")
                .build();

        kafkaTemplate.send(message);

        try {
            log.warn("Sending market data update response");

            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writeValueAsString(future.join());
            TransactionStatusDto result = mapper.readValue(jsonResult, TransactionStatusDto.class);

            if (result.getStatus()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result.getMessage());
            } else {
                return ResponseEntity.badRequest().body(result.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error in Kafka.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<Object> getMarketDataSpecific (String symbol, String source) {
        String correlationId = generateCorrelationId();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        GetSpecificRequest kafkaRequest = new GetSpecificRequest(correlationId, symbol, source);

        log.warn("Sending market data query specific request");

        Message<GetSpecificRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-query-specific")
                .build();

        kafkaTemplate.send(message);

        try {
            Object result = future.join();
            log.warn("Sending market data query specific response");

            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Market data not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error in Kafka.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<Object> getMarketDataConsolidated(String symbol) {
        String correlationId = generateCorrelationId();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        GetConsolidatedRequest kafkaRequest = new GetConsolidatedRequest(correlationId, symbol);

        log.warn("Sending market data query consolidated request");

        Message<GetConsolidatedRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-query-consolidated")
                .build();

        kafkaTemplate.send(message);

        try {
            Object result = future.join();
            log.warn("Sending market data query consolidated response");

            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Market data not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error in Kafka.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<Object> getMarketDataBatch(List<String> request) {
        String correlationId = generateCorrelationId();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        GetConsolidatedBatchRequest kafkaRequest = new GetConsolidatedBatchRequest(correlationId, request);

        log.warn("Sending market data query consolidated batch request");

        Message<GetConsolidatedBatchRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-query-consolidated-batch")
                .build();

        kafkaTemplate.send(message);

        try {
            Object result = future.join();
            log.warn("Sending market data query consolidated batch response");

            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Market data not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error in Kafka.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    public ResponseEntity<Object> deleteMarketData(String symbol, String source) {
        String correlationId = generateCorrelationId();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        DeleteRequest kafkaRequest = new DeleteRequest(correlationId, symbol, source);

        log.warn("Sending market data delete request");

        Message<DeleteRequest> message = MessageBuilder
                .withPayload(kafkaRequest)
                .setHeader(KafkaHeaders.TOPIC, "market-data-delete")
                .build();

        kafkaTemplate.send(message);

        try {
            log.warn("Sending market data delete response");

            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writeValueAsString(future.join());
            TransactionStatusDto result = mapper.readValue(jsonResult, TransactionStatusDto.class);

            if (result.getStatus()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(result.getMessage());
            } else {
                return ResponseEntity.badRequest().body(result.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error in Kafka.");
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    @KafkaListener(topics = "market-data-response", groupId = "market-data-api")
    public void handleCoreResponse(KafkaResponse response) {
        String correlationId = response.getCorrelationId();
        Object data = response.getData();

        log.warn("Received market data response");
        CompletableFuture<Object> future = pendingRequests.get(correlationId);

        if (future != null) {
            log.warn("Completing future");
            log.error("Data: {}", data);
            future.complete(data);
        }
    }
}
