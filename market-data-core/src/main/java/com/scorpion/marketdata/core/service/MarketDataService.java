package com.scorpion.marketdata.core.service;

import com.scorpion.marketdata.api.dto.MarketDataRequestBody;
import com.scorpion.marketdata.core.dto.MarketDataResponseBody;

import java.util.List;

public interface MarketDataService {
    boolean saveMarketData(MarketDataRequestBody marketData);
    MarketDataResponseBody getMarketDataSpecific(String symbol, String source);
    MarketDataResponseBody getMarketDataConsolidated(String symbol);
    List<MarketDataResponseBody> getMarketDataBatch(List<String> symbols);
    boolean deleteMarketData(String symbol, String source);
}
