package com.scorpion.marketdata.api.controller;

import com.scorpion.marketdata.api.broker.MarketDataApiBroker;
import com.scorpion.marketdata.api.dto.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Save market data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Market data saved successfully."),
            @ApiResponse(responseCode = "400", description = "The request is invalid. Try again."),
            @ApiResponse(responseCode = "500", description = "Something wrong with Kafka.")
    })
    public ResponseEntity<Object> saveMarketData(@RequestBody MarketDataRequestBody request) {
        ResponseEntity<Object> response = marketDataApiBroker.saveMarketData(request);
        log.info("Save request received by controller: {}", request);

        if (response != null) {
            log.info("Save response sent from controller: {}", response);
            return response;
        } else {
            log.error("Bad Save request in controller.");
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @GetMapping("/source")
    @Operation(summary = "Fetch market data for given a symbol and a source")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Market data successfully fetched."),
            @ApiResponse(responseCode = "400", description = "The request is invalid. Try again."),
            @ApiResponse(responseCode = "404", description = "Market data not found.")
    })
    public ResponseEntity<Object> getMarketDataSpecific(@RequestParam String symbol, @RequestParam String source) {
        ResponseEntity<Object> response = marketDataApiBroker.getMarketDataSpecific(symbol, source);
        log.info("Fetch specific request received by controller: {}, {}", symbol, response);

        if (response != null) {
            log.info("Fetch specific response sent from controller: {}", response);
            return response;
        } else {
            log.error("Bad Specific Fetch request in controller.");
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @GetMapping("/consolidated/{symbol}")
    @Operation(summary = "Fetch consolidated market data for a given symbol")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consolidated market data successfully fetched."),
            @ApiResponse(responseCode = "400", description = "The request is invalid. Try again."),
            @ApiResponse(responseCode = "404", description = "Market data not found."),
            @ApiResponse(responseCode = "500", description = "Something wrong with Kafka.")
    })
    public ResponseEntity<Object> getMarketDataConsolidated(@PathVariable String symbol) {
        ResponseEntity<Object> response = marketDataApiBroker.getMarketDataConsolidated(symbol);
        log.info("Fetch consolidated request received by controller: {}", symbol);

        if (response != null) {
            log.info("Fetch consolidated response sent from controller: {}", response);
            return response;
        } else {
            log.error("Bad Consolidated Fetch request in controller.");
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @GetMapping("/batch")
    @Operation(summary = "Fetch a batch of consolidated market data for a given list of symbols")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consolidated market data batch successfully fetched."),
            @ApiResponse(responseCode = "400", description = "The request is invalid. Try again."),
            @ApiResponse(responseCode = "404", description = "Market data not found."),
            @ApiResponse(responseCode = "500", description = "Something wrong with Kafka.")
    })
    public ResponseEntity<Object> getMarketDataBatch(@RequestBody List<String> request) {
        ResponseEntity<Object> response = marketDataApiBroker.getMarketDataBatch(request);
        log.info("Fetch a batch of request received by controller: {}", request);

        if (response != null) {
            log.info("Fetch a batch of response sent from controller: {}", response);
            return response;
        } else {
            log.error("Bad Batch Fetch request in controller.");
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }

    @DeleteMapping("/{symbol}/{source}")
    @Operation(summary = "Delete market data for given a symbol and a source")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Market data deleted successfully."),
            @ApiResponse(responseCode = "400", description = "The request is invalid. Try again."),
            @ApiResponse(responseCode = "500", description = "Something wrong with Kafka.")
    })
    public ResponseEntity<Object> deleteMarketData(@PathVariable String symbol, @PathVariable String source) {
        ResponseEntity<Object> response = marketDataApiBroker.deleteMarketData(symbol, source);
        log.info("Delete request received by controller: {}, {}", symbol, response);

        if (response != null) {
            log.info("Delete response sent from controller: {}", response);
            return response;
        } else {
            log.error("Bad Delete request in controller.");
            return ResponseEntity.badRequest().body("Bad HTTP request.");
        }
    }
}
