package com.scorpion.marketdata.core.service;

import com.scorpion.marketdata.api.dto.MarketDataRequestBody;
import com.scorpion.marketdata.core.dto.MarketDataResponseBody;
import com.scorpion.marketdata.core.dto.TransactionStatusDto;
import com.scorpion.marketdata.core.entity.MarketData;
import com.scorpion.marketdata.core.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MarketDataServiceImpl implements MarketDataService {
    private final PlatformTransactionManager transactionManager;
    private final MarketDataRepository marketDataRepository;

    public MarketDataServiceImpl(MarketDataRepository marketDataRepository, PlatformTransactionManager transactionManager) {
        this.marketDataRepository = marketDataRepository;
        this.transactionManager = transactionManager;
    }

    @Override
//    @CachePut(value = "save_market_data", key = "#marketData.symbol + #marketData.source")
    public TransactionStatusDto saveMarketData(MarketDataRequestBody marketData) {
        if (marketData == null) {
            return new TransactionStatusDto(false, "Invalid market data.");
        }

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            boolean marketDataExists = marketDataRepository.existsBySymbolAndSource(marketData.getSymbol(), marketData.getSource());

            if (!marketDataExists) {
                TransactionStatusDto insertMarketDataStatus = insertMarketData(marketData);

                if (!insertMarketDataStatus.getStatus()) {
                    transactionManager.rollback(status);
                    return insertMarketDataStatus;
                }

                boolean consolidatedMarketDataExists = marketDataRepository.existsBySymbolAndSource(marketData.getSymbol(), "CONSOLIDATED");

                if (!consolidatedMarketDataExists) {
                    TransactionStatusDto insertConsolidatedMarketDataStatus = insertConsolidatedMarketData(marketData);

                    if (!insertConsolidatedMarketDataStatus.getStatus()) {
                        transactionManager.rollback(status);
                        return insertConsolidatedMarketDataStatus;
                    }

                } else {
                    TransactionStatusDto updateConsolidatedMarketDataStatus = updateConsolidatedMarketData(marketData);

                    if (!updateConsolidatedMarketDataStatus.getStatus()) {
                        transactionManager.rollback(status);
                        return updateConsolidatedMarketDataStatus;
                    }
                }

            } else {
                TransactionStatusDto updateMarketDataStatus = updateMarketData(marketData);

                if (!updateMarketDataStatus.getStatus()) {
                    transactionManager.rollback(status);
                    return updateMarketDataStatus;
                }

                boolean consolidatedMarketDataExists = marketDataRepository.existsBySymbolAndSource(marketData.getSymbol(), "CONSOLIDATED");

                if (!consolidatedMarketDataExists) {
                    TransactionStatusDto updateConsolidatedMarketDataStatus = updateConsolidatedMarketData(marketData);

                    if (!updateConsolidatedMarketDataStatus.getStatus()) {
                        transactionManager.rollback(status);
                        return updateConsolidatedMarketDataStatus;
                    }
                }
            }

            // Commit transaction if all operations succeed
            transactionManager.commit(status);
            return new TransactionStatusDto(true, "Market data saved successfully.");

        } catch (Exception e) {
            transactionManager.rollback(status);
            return new TransactionStatusDto(false, "Error saving market data.");
        }
    }

    @Override
//    @Cacheable(value = "get_market_data_specific", key = "#symbol + #source")
    public MarketDataResponseBody getMarketDataSpecific(String symbol, String source) {
        if (symbol == null  || source == null) {
            return null;
        }

        MarketData entity = marketDataRepository.findBySymbolAndSource(symbol, source);

        if (entity == null) {
            return null;
        }

        return new MarketDataResponseBody(
                entity.getSymbol(),
                entity.getLastTradedPrice(),
                entity.getBidPrice(),
                entity.getMidPrice(),
                entity.getAskPrice(),
                entity.getMarketTimestamp(),
                entity.getDependsOnSymbol(),
                entity.getSource(),
                entity.getLastCouponDate().toString(),
                entity.getInterestRate(),
                entity.getVolatility(),
                entity.getAccruedInterest(),
                entity.getTheoreticalPrice()
        );
    }

    @Override
//    @Cacheable(value = "get_market_data_consolidated", key = "#symbol ")
    public MarketDataResponseBody getMarketDataConsolidated(String symbol) {
        if (symbol == null) {
            return null;
        }

        MarketData entity = marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED");

        if (entity == null) {
            return null;
        }

        return new MarketDataResponseBody(
                entity.getSymbol(),
                entity.getLastTradedPrice(),
                entity.getBidPrice(),
                entity.getMidPrice(),
                entity.getAskPrice(),
                entity.getMarketTimestamp(),
                entity.getDependsOnSymbol(),
                entity.getSource(),
                entity.getLastCouponDate().toString(),
                entity.getInterestRate(),
                entity.getVolatility(),
                entity.getAccruedInterest(),
                entity.getTheoreticalPrice()
        );
    }

    @Override
//    @Cacheable(value = "get_market_data_batch", key = "#symbols")
    public List<MarketDataResponseBody> getMarketDataBatch(List<String> symbols) {
        if (symbols == null) {
            return null;
        }

        List<MarketDataResponseBody> marketDataList = new ArrayList<>();

        for (String symbol : symbols) {
            if (symbol == null) {
                marketDataList.add(null);
                continue;
            }

            MarketData entity = marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED");

            if (entity == null) {
                marketDataList.add(null);
                continue;
            }

            marketDataList.add(new MarketDataResponseBody(
                    entity.getSymbol(),
                    entity.getLastTradedPrice(),
                    entity.getBidPrice(),
                    entity.getMidPrice(),
                    entity.getAskPrice(),
                    entity.getMarketTimestamp(),
                    entity.getDependsOnSymbol(),
                    entity.getSource(),
                    entity.getLastCouponDate().toString(),
                    entity.getInterestRate(),
                    entity.getVolatility(),
                    entity.getAccruedInterest(),
                    entity.getTheoreticalPrice()
            ));
        }

        return marketDataList;
    }

    @Override
//    @CacheEvict(value = "delete_market_data", key = "#symbol + #source")
    public TransactionStatusDto deleteMarketData(String symbol, String source) {
        if (symbol == null || source == null) {
            return new TransactionStatusDto(false, "Invalid symbol or source.");
        }

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // Check if market data exists
            if (!marketDataRepository.existsBySymbolAndSource(symbol, source)) {
                transactionManager.rollback(status);
                return new TransactionStatusDto(false, "Market data does not exist.");
            }

            // Delete market data
            TransactionStatusDto deleteMarketDataStatus = deleteMarketDataRecord(symbol, source);

            if (!deleteMarketDataStatus.getStatus()) {
                transactionManager.rollback(status);
                return deleteMarketDataStatus;
            }

            // Check if no other market data exists except "CONSOLIDATED"
            boolean noMarketDataExists = marketDataRepository
                    .findAllBySymbolAndSourceIsNotIn(symbol, Collections.singletonList("CONSOLIDATED"))
                    .isEmpty();

            if (noMarketDataExists) {
                // Check if "CONSOLIDATED" market data exists
                if (marketDataRepository.existsBySymbolAndSource(symbol, "CONSOLIDATED")) {
                    // Delete "CONSOLIDATED" market data
                    TransactionStatusDto deleteConsolidatedMarketDataStatus = deleteConsolidatedMarketData(symbol);

                    if (!deleteConsolidatedMarketDataStatus.getStatus()) {
                        transactionManager.rollback(status);
                        return deleteConsolidatedMarketDataStatus;
                    }

                    // Check if dependent market data exists
                    if (marketDataRepository.existsByDependsOnSymbol(symbol)) {
                        TransactionStatusDto updateDependantMarketDataStatus = updateDependantMarketData(symbol);

                        if (!updateDependantMarketDataStatus.getStatus()) {
                            transactionManager.rollback(status);
                            return updateDependantMarketDataStatus;
                        }
                    }
                }
            } else {
                // Re-consolidate market data if other market data exists
                TransactionStatusDto reConsolidateMarketDataStatus = reConsolidateMarketData(symbol);

                if (!reConsolidateMarketDataStatus.getStatus()) {
                    transactionManager.rollback(status);
                    return reConsolidateMarketDataStatus;
                }
            }

            // Commit transaction if all operations succeed
            transactionManager.commit(status);
            return new TransactionStatusDto(true, "Market data deleted successfully.");

        } catch (Exception e) {
            transactionManager.rollback(status);
            return new TransactionStatusDto(false, "Error deleting market data.");
        }
    }

    private TransactionStatusDto insertMarketData(MarketDataRequestBody marketData) {
        double accruedInterest = calculateAccruedInterest(marketData.getLastTradedPrice(), marketData.getInterestRate(), marketData.getLastCouponDate());
        double theoreticalPrice = calculateTheoreticalPrice(marketData.getDependsOnSymbol(), marketData.getVolatility());

        marketDataRepository.save(new MarketData(
                marketData.getSymbol(),
                marketData.getLastTradedPrice(),
                marketData.getBidPrice(),
                marketData.getMidPrice(),
                marketData.getAskPrice(),
                marketData.getMarketTimestamp(),
                marketData.getDependsOnSymbol(),
                marketData.getSource(),
                marketData.getLastCouponDate(),
                marketData.getInterestRate(),
                marketData.getVolatility(),
                accruedInterest,
                theoreticalPrice
        ));

        MarketData savedMarketData = marketDataRepository.findBySymbolAndSource(marketData.getSymbol(), marketData.getSource());

        boolean isMarketDataInserted = savedMarketData != null;
        return new TransactionStatusDto(isMarketDataInserted, isMarketDataInserted ? "Market data inserted successfully." : "Failed to insert market data.");
    }

    private TransactionStatusDto updateMarketData(MarketDataRequestBody marketData) {
        MarketData existingMarketData = marketDataRepository.findBySymbolAndSource(marketData.getSymbol(), marketData.getSource());

        if (marketData.getLastTradedPrice() != null) {
            existingMarketData.setLastTradedPrice(marketData.getLastTradedPrice());
        }

        if (marketData.getBidPrice() != null) {
            existingMarketData.setBidPrice(marketData.getBidPrice());
        }

        if (marketData.getMidPrice() != null) {
            existingMarketData.setMidPrice(marketData.getMidPrice());
        }

        if (marketData.getAskPrice() != null) {
            existingMarketData.setAskPrice(marketData.getAskPrice());
        }

        if (marketData.getMarketTimestamp() != null) {
            existingMarketData.setMarketTimestamp(marketData.getMarketTimestamp());
        }

        if (marketData.getDependsOnSymbol() != null) {
            existingMarketData.setDependsOnSymbol(marketData.getDependsOnSymbol());
        }

        if (marketData.getLastCouponDate() != null) {
            existingMarketData.setLastCouponDate(marketData.getLastCouponDate());
        }

        if (marketData.getInterestRate() != null) {
            existingMarketData.setInterestRate(marketData.getInterestRate());
        }

        if (marketData.getVolatility() != null) {
            existingMarketData.setVolatility(marketData.getVolatility());
        }

        double accruedInterest = calculateAccruedInterest(existingMarketData.getLastTradedPrice(), existingMarketData.getInterestRate(), existingMarketData.getLastCouponDate());
        double theoreticalPrice = calculateTheoreticalPrice(existingMarketData.getDependsOnSymbol(), existingMarketData.getVolatility());

        existingMarketData.setAccruedInterest(accruedInterest);
        existingMarketData.setTheoreticalPrice(theoreticalPrice);

        marketDataRepository.save(existingMarketData);

        MarketData savedMarketData = marketDataRepository.findBySymbolAndSource(marketData.getSymbol(), marketData.getSource());

        boolean isMarketDataUpdated = savedMarketData != null;
        return new TransactionStatusDto(isMarketDataUpdated, isMarketDataUpdated ? "Market data updated successfully." : "Failed to update market data.");
    }

    private TransactionStatusDto insertConsolidatedMarketData(MarketDataRequestBody marketData) {
        marketDataRepository.save(new MarketData(
                marketData.getSymbol(),
                marketData.getLastTradedPrice(),
                marketData.getBidPrice(),
                marketData.getMidPrice(),
                marketData.getAskPrice(),
                marketData.getMarketTimestamp(),
                marketData.getDependsOnSymbol(),
                "CONSOLIDATED",
                marketData.getLastCouponDate(),
                marketData.getInterestRate(),
                marketData.getVolatility(),
                calculateAccruedInterest(marketData.getLastTradedPrice(), marketData.getInterestRate(), marketData.getLastCouponDate()),
                calculateTheoreticalPrice(marketData.getDependsOnSymbol(), marketData.getVolatility())
        ));

        MarketData savedConsolidatedMarketData = marketDataRepository.findBySymbolAndSource(marketData.getSymbol(), "CONSOLIDATED");

        boolean isMarketDataInserted = savedConsolidatedMarketData != null;
        return new TransactionStatusDto(isMarketDataInserted, isMarketDataInserted ? "Consolidated market data inserted successfully." : "Failed to insert consolidated market data.");
    }

    private TransactionStatusDto updateConsolidatedMarketData(MarketDataRequestBody marketData) {
        MarketData existingConsolidatedMarketData = marketDataRepository.findBySymbolAndSource(marketData.getSymbol(), "CONSOLIDATED");

        if (marketData.getLastTradedPrice() != null) {
            existingConsolidatedMarketData.setLastTradedPrice(marketData.getLastTradedPrice());
        }

        if (marketData.getBidPrice() != null) {
            existingConsolidatedMarketData.setBidPrice(marketData.getBidPrice());
        }

        if (marketData.getMidPrice() != null) {
            existingConsolidatedMarketData.setMidPrice(marketData.getMidPrice());
        }

        if (marketData.getAskPrice() != null) {
            existingConsolidatedMarketData.setAskPrice(marketData.getAskPrice());
        }

        if (marketData.getMarketTimestamp() != null) {
            existingConsolidatedMarketData.setMarketTimestamp(marketData.getMarketTimestamp());
        }

        if (marketData.getDependsOnSymbol() != null) {
            existingConsolidatedMarketData.setDependsOnSymbol(marketData.getDependsOnSymbol());
        }

        if (marketData.getLastCouponDate() != null) {
            existingConsolidatedMarketData.setLastCouponDate(marketData.getLastCouponDate());
        }

        if (marketData.getInterestRate() != null) {
            existingConsolidatedMarketData.setInterestRate(marketData.getInterestRate());
        }

        if (marketData.getVolatility() != null) {
            existingConsolidatedMarketData.setVolatility(marketData.getVolatility());
        }

        double accruedInterest = calculateAccruedInterest(existingConsolidatedMarketData.getLastTradedPrice(), existingConsolidatedMarketData.getInterestRate(), existingConsolidatedMarketData.getLastCouponDate());
        double theoreticalPrice = calculateTheoreticalPrice(existingConsolidatedMarketData.getDependsOnSymbol(), existingConsolidatedMarketData.getVolatility());

        existingConsolidatedMarketData.setAccruedInterest(accruedInterest);
        existingConsolidatedMarketData.setTheoreticalPrice(theoreticalPrice);

        marketDataRepository.save(existingConsolidatedMarketData);

        MarketData savedConsolidatedMarketData = marketDataRepository.findBySymbolAndSource(marketData.getSymbol(), "CONSOLIDATED");

        boolean isMarketDataUpdated = savedConsolidatedMarketData != null;
        return new TransactionStatusDto(isMarketDataUpdated, isMarketDataUpdated ? "Consolidated market data updated successfully." : "Failed to update consolidated market data.");
    }

    private TransactionStatusDto updateDependantMarketData(String symbol) {
        boolean dependantMarketDataExists = marketDataRepository.existsByDependsOnSymbol(symbol);

        if (!dependantMarketDataExists) {
            return new TransactionStatusDto(false, "No dependant market data found.");
        }

        List<MarketData> dependantMarketDataList = marketDataRepository.findAllByDependsOnSymbol(symbol);

        if (dependantMarketDataList == null || dependantMarketDataList.isEmpty()) {
            return new TransactionStatusDto(false, "No dependant market data found.");
        }

        for (MarketData dependantMarketData : dependantMarketDataList) {
            dependantMarketData.setDependsOnSymbol(null);

            double theoreticalPrice = calculateTheoreticalPrice(dependantMarketData.getDependsOnSymbol(), dependantMarketData.getVolatility());

            dependantMarketData.setTheoreticalPrice(theoreticalPrice);

            marketDataRepository.save(dependantMarketData);

            MarketData savedDependantMarketData = marketDataRepository.findBySymbolAndSource(dependantMarketData.getSymbol(), dependantMarketData.getSource());

            if (savedDependantMarketData.getDependsOnSymbol() != null) {
                return new TransactionStatusDto(false, "Failed to update dependant market data.");
            }
        }

        return new TransactionStatusDto(true, "Dependant market data updated successfully.");
    }

    private TransactionStatusDto deleteMarketDataRecord(String symbol, String source) {
        boolean marketDataExists = marketDataRepository.existsBySymbolAndSource(symbol, source);

        if (!marketDataExists) {
            return new TransactionStatusDto(false, "No market data found.");
        }

        marketDataRepository.deleteBySymbolAndSource(symbol, source);

        boolean isMarketDataDeleted = !marketDataRepository.existsBySymbolAndSource(symbol, source);
        return new TransactionStatusDto(isMarketDataDeleted, isMarketDataDeleted ? "Market data deleted successfully." : "Failed to delete market data.");
    }

    private TransactionStatusDto deleteConsolidatedMarketData(String symbol) {
        boolean consolidatedMarketDataExists = marketDataRepository.existsBySymbolAndSource(symbol, "CONSOLIDATED");

        if (!consolidatedMarketDataExists) {
            return new TransactionStatusDto(false, "No consolidated market data found.");
        }

        marketDataRepository.deleteBySymbolAndSource(symbol, "CONSOLIDATED");

        boolean isConsolidatedMarketDataDeleted = !marketDataRepository.existsBySymbolAndSource(symbol, "CONSOLIDATED");
        return new TransactionStatusDto(isConsolidatedMarketDataDeleted, isConsolidatedMarketDataDeleted ? "Consolidated market data deleted successfully." : "Failed to delete consolidated market data.");
    }

    private TransactionStatusDto reConsolidateMarketData(String symbol) {
        List<MarketData> marketDataList = marketDataRepository.findAllBySymbolAndSourceIsNotInOrderByMarketTimestampDesc(symbol, Collections.singletonList("CONSOLIDATED"));

        if (marketDataList == null || marketDataList.isEmpty()) {
            return new TransactionStatusDto(false, "No market data found.");
        }

        MarketData existingConsolidatedMarketData = marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED");

        if (existingConsolidatedMarketData == null) {
            return new TransactionStatusDto(false, "No consolidated market data found.");
        }

        MarketData newConsolidatedMarketData = new MarketData();
        newConsolidatedMarketData.setId(existingConsolidatedMarketData.getId());
        newConsolidatedMarketData.setSymbol(symbol);
        newConsolidatedMarketData.setSource("CONSOLIDATED");

        for (MarketData marketData : marketDataList) {
            newConsolidatedMarketData.setLastTradedPrice(marketData.getLastTradedPrice());
            newConsolidatedMarketData.setBidPrice(marketData.getBidPrice());
            newConsolidatedMarketData.setMidPrice(marketData.getMidPrice());
            newConsolidatedMarketData.setAskPrice(marketData.getAskPrice());
            newConsolidatedMarketData.setMarketTimestamp(marketData.getMarketTimestamp());
            newConsolidatedMarketData.setDependsOnSymbol(marketData.getDependsOnSymbol());
            newConsolidatedMarketData.setLastCouponDate(marketData.getLastCouponDate());
            newConsolidatedMarketData.setInterestRate(marketData.getInterestRate());
            newConsolidatedMarketData.setVolatility(marketData.getVolatility());
            newConsolidatedMarketData.setAccruedInterest(marketData.getAccruedInterest());
            newConsolidatedMarketData.setTheoreticalPrice(marketData.getTheoreticalPrice());

            if (newConsolidatedMarketData.getLastTradedPrice() != null &&
                    newConsolidatedMarketData.getBidPrice() != null &&
                    newConsolidatedMarketData.getMidPrice() != null &&
                    newConsolidatedMarketData.getAskPrice() != null &&
                    newConsolidatedMarketData.getMarketTimestamp() != null &&
                    newConsolidatedMarketData.getDependsOnSymbol() != null &&
                    newConsolidatedMarketData.getLastCouponDate() != null &&
                    newConsolidatedMarketData.getInterestRate() != null &&
                    newConsolidatedMarketData.getVolatility() != null &&
                    newConsolidatedMarketData.getAccruedInterest() != null &&
                    newConsolidatedMarketData.getTheoreticalPrice() != null) {
                break;
            }
        }

        marketDataRepository.save(newConsolidatedMarketData);

        MarketData savedConsolidatedMarketData = marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED");
        boolean isMarketDataReConsolidated = savedConsolidatedMarketData != null;

        return new TransactionStatusDto(isMarketDataReConsolidated, isMarketDataReConsolidated ? "Market data re-consolidated successfully." : "Failed to re-consolidate market data.");
    }

    private double calculateAccruedInterest(Double lastTradedPrice, Double interestRate, LocalDate lastCouponDate) {
        LocalDate currentDate = LocalDate.now();

        double accruedInterest = 0.0;

        if (lastTradedPrice!=null && interestRate!=null && lastCouponDate!=null) {
            long daysBetween = ChronoUnit.DAYS.between(lastCouponDate, currentDate);
            accruedInterest = lastTradedPrice * interestRate / daysBetween;
        }

        return accruedInterest;
    }

    private double calculateTheoreticalPrice(String dependsOnSymbol, Double volatility) {
        double theoreticalPrice = 0.0;

        if (dependsOnSymbol != null) {
            MarketData dependsOn = marketDataRepository.findBySymbolAndSource(dependsOnSymbol, "CONSOLIDATED");
            if (dependsOn != null && dependsOn.getLastTradedPrice() != null && volatility != null) {
                theoreticalPrice = dependsOn.getLastTradedPrice() * volatility;
            }
        }

        return theoreticalPrice;
    }
}
