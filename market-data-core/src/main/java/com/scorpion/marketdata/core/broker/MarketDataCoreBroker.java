package com.scorpion.marketdata.core.broker;

import com.scorpion.marketdata.api.dto.*;
import com.scorpion.marketdata.core.service.MarketDataService;
import com.scorpion.marketdata.core.service.MarketDataServiceImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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

        TempPair kafkaResponse = new TempPair(correlationId, marketDataRequestBody !=null ? "Update successful" : "Update failed");

        log.warn("Received market data update request");

        kafkaTemplate.send("market-data-response", kafkaResponse);
        log.warn("Processing market data update response");
    }

    @KafkaListener(topics = "market-data-query-specific", groupId = "market-data-core")
    public void handleMarketDataQuerySpecific(GetSpecificRequest request) {
        String correlationId = request.getCorrelationId();
        String symbol = request.getSymbol();
        String source = request.getSource();

        TempPair kafkaResponse = new TempPair(correlationId, String.format("PROCESSED SPECIFIC DATA :: [symbol:%s, source:%s]", symbol, source));

        log.warn("Received market data query specific request");

        kafkaTemplate.send("market-data-response", kafkaResponse);
        log.warn("Processing market data query specific response");
    }

    @KafkaListener(topics = "market-data-query-consolidated", groupId = "market-data-core")
    public void handleMarketDataQueryConsolidated(GetConsolidatedRequest request) {
        String correlationId = request.getCorrelationId();
        String symbol = request.getSymbol();

        TempPair kafkaResponse = new TempPair(correlationId, String.format("PROCESSED CONSOLIDATED DATA :: [symbol:%s]", symbol));

        log.warn("Received market data query consolidated request");

        kafkaTemplate.send("market-data-response", kafkaResponse);
        log.warn("Processing market data query consolidated response");
    }

    @KafkaListener(topics = "market-data-query-consolidated-batch", groupId = "market-data-core")
    public void handleMarketDataQueryConsolidatedBatch(GetConsolidatedBatchRequest request) {
        String correlationId = request.getCorrelationId();
        List<String> symbols = request.getSymbol();

        /*for (String symbol : symbols) {
            log.warn("Processing symbol: " + symbol);
        }*/

        TempPair kafkaResponse = new TempPair(correlationId, String.format("PROCESSED BATCH DATA :: [symbols:(%s)]", String.join("/", symbols)));

        log.warn("Received market data query consolidated batch request");

        kafkaTemplate.send("market-data-response", kafkaResponse);
        log.warn("Processing market data query consolidated batch response");
    }

    @KafkaListener(topics = "market-data-delete", groupId = "market-data-core")
    public void handleMarketDataDelete(DeleteRequest request) {
        String correlationId = request.getCorrelationId();
        String symbol = request.getSymbol();
        String source = request.getSource();

        TempPair kafkaResponse = new TempPair(correlationId, (symbol!=null && source!=null) ? "Delete successful" : "Delete failed");

        log.warn("Received market data delete request");

        kafkaTemplate.send("market-data-response", kafkaResponse);
        log.warn("Processing market data delete response");
    }
}
