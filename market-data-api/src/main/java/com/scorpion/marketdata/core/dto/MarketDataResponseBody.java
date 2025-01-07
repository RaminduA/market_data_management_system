package com.scorpion.marketdata.core.dto;

import java.time.LocalDate;

public class MarketDataResponseBody {
    private final String symbol;
    private final Double lastTradedPrice;
    private final Double bidPrice;
    private final Double midPrice;
    private final Double askPrice;
    private final Long marketTimestamp;
    private final String dependsOnSymbol;
    private final String source;
    private final String lastCouponDate;
    private final Double interestRate;
    private final Double volatility;
    private final Double accruedInterest;
    private final Double theoreticalPrice;

    public MarketDataResponseBody(String symbol, Double lastTradedPrice, Double bidPrice, Double midPrice, Double askPrice, Long marketTimestamp, String dependsOnSymbol, String source, String lastCouponDate, Double interestRate, Double volatility, Double accruedInterest, Double theoreticalPrice) {
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
        this.accruedInterest = accruedInterest;
        this.theoreticalPrice = theoreticalPrice;
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

    public Long getMarketTimestamp() {
        return marketTimestamp;
    }

    public String getDependsOnSymbol() {
        return dependsOnSymbol;
    }

    public String getSource() {
        return source;
    }

    public String getLastCouponDate() {
        return lastCouponDate;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public Double getVolatility() {
        return volatility;
    }

    public Double getAccruedInterest() {
        return accruedInterest;
    }

    public Double getTheoreticalPrice() {
        return theoreticalPrice;
    }
}
