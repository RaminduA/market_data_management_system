package com.scorpion.marketdata.core.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "market_data")
public class MarketData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String symbol;
    private Double lastTradedPrice;
    private Double bidPrice;
    private Double midPrice;
    private Double askPrice;
    private Long marketTimestamp;
    private String dependsOnSymbol;
    private String source;
    private LocalDate lastCouponDate;
    private Double interestRate;
    private Double volatility;
    private Double accruedInterest;
    private Double theoreticalPrice;

    public MarketData() {
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
        this.accruedInterest = null;
        this.theoreticalPrice = null;
    }

    public MarketData(String symbol, Double lastTradedPrice, Double bidPrice, Double midPrice, Double askPrice, Long marketTimestamp, String dependsOnSymbol, String source, LocalDate lastCouponDate, Double interestRate, Double volatility, Double accruedInterest, Double theoreticalPrice) {
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
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getLastTradedPrice() {
        return lastTradedPrice;
    }

    public void setLastTradedPrice(Double lastTradedPrice) {
        this.lastTradedPrice = lastTradedPrice;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public Double getMidPrice() {
        return midPrice;
    }

    public void setMidPrice(Double midPrice) {
        this.midPrice = midPrice;
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public Long getMarketTimestamp() {
        return marketTimestamp;
    }

    public void setMarketTimestamp(Long marketTimestamp) {
        this.marketTimestamp = marketTimestamp;
    }

    public String getDependsOnSymbol() {
        return dependsOnSymbol;
    }

    public void setDependsOnSymbol(String dependsOnSymbol) {
        this.dependsOnSymbol = dependsOnSymbol;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDate getLastCouponDate() {
        return lastCouponDate;
    }

    public void setLastCouponDate(LocalDate lastCouponDate) {
        this.lastCouponDate = lastCouponDate;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Double getVolatility() {
        return volatility;
    }

    public void setVolatility(Double volatility) {
        this.volatility = volatility;
    }

    public Double getAccruedInterest() {
        return accruedInterest;
    }

    public void setAccruedInterest(Double accruedInterest) {
        this.accruedInterest = accruedInterest;
    }

    public Double getTheoreticalPrice() {
        return theoreticalPrice;
    }

    public void setTheoreticalPrice(Double theoreticalPrice) {
        this.theoreticalPrice = theoreticalPrice;
    }
}
