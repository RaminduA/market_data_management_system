package com.scorpion.marketdata.api.dto;

import java.time.LocalDate;

public class MarketDataRequestBody {
    private final String symbol;
    private final Double lastTradedPrice;
    private final Double bidPrice;
    private final Double midPrice;
    private final Double askPrice;
    private final String marketTimestamp;
    private final String dependsOnSymbol;
    private final String source;
    private final LocalDate lastCouponDate;
    private final Double interestRate;
    private final Double volatility;

    public MarketDataRequestBody() {
        this.symbol = null;
        this.lastTradedPrice = null;
        this.bidPrice = null;
        this.midPrice = null;
        this.askPrice = null;
        this.marketTimestamp = null;
        this.dependsOnSymbol = null;
        this.source = null;
        this.lastCouponDate = null;
        this.interestRate = null;
        this.volatility = null;
    }

    public MarketDataRequestBody(String symbol, Double lastTradedPrice, Double bidPrice, Double midPrice, Double askPrice, String marketTimestamp, String dependsOnSymbol, String source, LocalDate lastCouponDate, Double interestRate, Double volatility) {
        this.symbol = symbol;
        this.lastTradedPrice = lastTradedPrice;
        this.bidPrice = bidPrice;
        this.midPrice = midPrice;
        this.askPrice = askPrice;
        this.marketTimestamp = marketTimestamp;
        this.dependsOnSymbol = dependsOnSymbol;
        this.source = source;
        this.lastCouponDate = lastCouponDate;
        this.interestRate = interestRate;
        this.volatility = volatility;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getLastTradedPrice() {
        return lastTradedPrice;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public Double getMidPrice() {
        return midPrice;
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public String getMarketTimestamp() {
        return marketTimestamp;
    }

    public String getDependsOnSymbol() {
        return dependsOnSymbol;
    }

    public String getSource() {
        return source;
    }

    public LocalDate getLastCouponDate() {
        return lastCouponDate;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public Double getVolatility() {
        return volatility;
    }

    public String toString() {
        return "MarketData{" + '\n' +
                "symbol='" + symbol + '\'' + ", " +  '\n' +
                "lastTradedPrice=" + lastTradedPrice + ", " +  '\n' +
                "bidPrice=" + bidPrice + ", " +  '\n' +
                "midPrice=" + midPrice + ", " +  '\n' +
                "askPrice=" + askPrice + ", " +  '\n' +
                "marketTimestamp=" + marketTimestamp + ", " +  '\n' +
                "dependsOnSymbol='" + dependsOnSymbol + '\'' + ", " +  '\n' +
                "source='" + source + '\'' + ", " +  '\n' +
                "lastCouponDate=" + lastCouponDate + ", " + '\n' +
                "interestRate=" + interestRate + ", " +  '\n' +
                "volatility=" + volatility + '\n' +
                '}';
    }

}
