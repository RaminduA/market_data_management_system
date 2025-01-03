package com.scorpion.marketdata.api.controller;

import com.scorpion.marketdata.api.broker.MarketDataApiBroker;
import com.scorpion.marketdata.api.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/market-data")
public class MarketDataController {
    private static final Logger log = LoggerFactory.getLogger(MarketDataController.class);
    private final MarketDataApiBroker marketDataApiBroker;

    public MarketDataController(MarketDataApiBroker marketDataApiBroker) {
        this.marketDataApiBroker = marketDataApiBroker;
    }

    @PostMapping
    public ResponseEntity<String> addMarketData(@RequestBody MarketDataDto request) {
        ResponseEntity<String> response = marketDataApiBroker.addMarketData(request);

        if (response != null) {
            return response;
        } else {
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @GetMapping("/source")
    public ResponseEntity<String> getMarketDataSpecific(@RequestParam String symbol, @RequestParam String source) {
        ResponseEntity<String> response = marketDataApiBroker.getMarketDataSpecific(symbol, source);

        if (response != null) {
            return response;
        } else {
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @GetMapping("/consolidated/{symbol}")
    public ResponseEntity<String> getMarketDataConsolidated(@PathVariable String symbol) {
        ResponseEntity<String> response = marketDataApiBroker.getMarketDataConsolidated(symbol);

        if (response != null) {
            return response;
        } else {
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @GetMapping("/batch")
    public ResponseEntity<String> getMarketDataBatch(@RequestBody List<String> request) {
        ResponseEntity<String> response = marketDataApiBroker.getMarketDataBatch(request);

        if (response != null) {
            return response;
        } else {
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @DeleteMapping("/{symbol}/{source}")
    public ResponseEntity<String> deleteMarketData(@PathVariable String symbol, @PathVariable String source) {
        ResponseEntity<String> response = marketDataApiBroker.deleteMarketData(symbol, source);

        if (response != null) {
            return response;
        } else {
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }
}
