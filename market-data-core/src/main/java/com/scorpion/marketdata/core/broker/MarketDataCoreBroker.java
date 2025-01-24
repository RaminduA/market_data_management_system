package com.scorpion.marketdata.core.broker;

import com.scorpion.marketdata.api.dto.*;
import com.scorpion.marketdata.core.dto.KafkaResponse;
import com.scorpion.marketdata.core.service.MarketDataService;
import com.scorpion.marketdata.core.service.MarketDataServiceImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class MarketDataCoreBroker {
    private static final Logger log = LoggerFactory.getLogger(MarketDataCoreBroker.class);
    private final MarketDataService marketDataService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MarketDataCoreBroker(MarketDataServiceImpl marketDataService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.marketDataService = marketDataService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "market-data-update", groupId = "market-data-core")
    public void handleMarketDataUpdate(UpdateRequest request) {
        String correlationId = request.getCorrelationId();
        MarketDataRequestBody marketDataRequestBody = request.getMarketData();

        KafkaResponse kafkaResponse = new KafkaResponse(correlationId, marketDataService.saveMarketData(marketDataRequestBody));

        log.info("Received market data update request");

        Message<KafkaResponse> message = MessageBuilder
                .withPayload(kafkaResponse)
                .setHeader(KafkaHeaders.TOPIC, "market-data-response")
                .build();

        kafkaTemplate.send(message);
        log.info("Processing market data update response");
    }

    @KafkaListener(topics = "market-data-query-specific", groupId = "market-data-core")
    public void handleMarketDataQuerySpecific(GetSpecificRequest request) {
        String correlationId = request.getCorrelationId();
        String symbol = request.getSymbol();
        String source = request.getSource();

        KafkaResponse kafkaResponse = new KafkaResponse(correlationId, marketDataService.getMarketDataSpecific(symbol, source));

        log.info("Received market data query specific request");

        Message<KafkaResponse> message = MessageBuilder
                .withPayload(kafkaResponse)
                .setHeader(KafkaHeaders.TOPIC, "market-data-response")
                .build();

        kafkaTemplate.send(message);
        log.info("Processing market data query specific response");
    }

    @KafkaListener(topics = "market-data-query-consolidated", groupId = "market-data-core")
    public void handleMarketDataQueryConsolidated(GetConsolidatedRequest request) {
        String correlationId = request.getCorrelationId();
        String symbol = request.getSymbol();

        KafkaResponse kafkaResponse = new KafkaResponse(correlationId, marketDataService.getMarketDataConsolidated(symbol));

        log.info("Received market data query consolidated request");

        Message<KafkaResponse> message = MessageBuilder
                .withPayload(kafkaResponse)
                .setHeader(KafkaHeaders.TOPIC, "market-data-response")
                .build();

        kafkaTemplate.send(message);
        log.info("Processing market data query consolidated response");
    }

    @KafkaListener(topics = "market-data-query-consolidated-batch", groupId = "market-data-core")
    public void handleMarketDataQueryConsolidatedBatch(GetConsolidatedBatchRequest request) {
        String correlationId = request.getCorrelationId();
        List<String> symbols = request.getSymbol();

        KafkaResponse kafkaResponse = new KafkaResponse(correlationId, marketDataService.getMarketDataBatch(symbols));

        log.info("Received market data query consolidated batch request");

        Message<KafkaResponse> message = MessageBuilder
                .withPayload(kafkaResponse)
                .setHeader(KafkaHeaders.TOPIC, "market-data-response")
                .build();

        kafkaTemplate.send(message);
        log.info("Processing market data query consolidated batch response");
    }

    @KafkaListener(topics = "market-data-delete", groupId = "market-data-core")
    public void handleMarketDataDelete(DeleteRequest request) {
        String correlationId = request.getCorrelationId();
        String symbol = request.getSymbol();
        String source = request.getSource();

        KafkaResponse kafkaResponse = new KafkaResponse(correlationId, marketDataService.deleteMarketData(symbol, source));

        log.info("Received market data delete request");

        Message<KafkaResponse> message = MessageBuilder
                .withPayload(kafkaResponse)
                .setHeader(KafkaHeaders.TOPIC, "market-data-response")
                .build();

        kafkaTemplate.send(message);
        log.info("Processing market data delete response");
    }
}
