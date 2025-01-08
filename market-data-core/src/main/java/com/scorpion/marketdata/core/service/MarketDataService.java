package com.scorpion.marketdata.core.service;

import com.scorpion.marketdata.api.dto.MarketDataRequestBody;
import com.scorpion.marketdata.core.dto.MarketDataResponseBody;
import com.scorpion.marketdata.core.dto.TransactionStatusDto;

import java.util.List;

public interface MarketDataService {
    TransactionStatusDto saveMarketData(MarketDataRequestBody marketData);
    MarketDataResponseBody getMarketDataSpecific(String symbol, String source);
    MarketDataResponseBody getMarketDataConsolidated(String symbol);
    List<MarketDataResponseBody> getMarketDataBatch(List<String> symbols);
    TransactionStatusDto deleteMarketData(String symbol, String source);
}
