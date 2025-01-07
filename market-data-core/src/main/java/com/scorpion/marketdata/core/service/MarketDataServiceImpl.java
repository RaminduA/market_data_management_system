package com.scorpion.marketdata.core.service;

import com.scorpion.marketdata.api.dto.MarketDataRequestBody;
import com.scorpion.marketdata.core.dto.MarketDataResponseBody;
import com.scorpion.marketdata.core.entity.MarketData;
import com.scorpion.marketdata.core.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MarketDataServiceImpl implements MarketDataService {
    private final MarketDataRepository marketDataRepository;

    public MarketDataServiceImpl(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }

    @Override
    public boolean saveMarketData(MarketDataRequestBody marketData) {
        if (marketData == null) {
            return false;
        }

        boolean marketDataExists = marketDataRepository.existsBySymbolAndSource(marketData.getSymbol(), marketData.getSource());

        if (!marketDataExists) {
            boolean isMarketDataInserted = insertMarketData(marketData);

            if (isMarketDataInserted) {
                boolean consolidatedMarketDataExists = marketDataRepository.existsBySymbolAndSource(marketData.getSymbol(), "CONSOLIDATED");

                if (!consolidatedMarketDataExists) {
                    return insertConsolidatedMarketData(marketData);
                } else {
                    return false;
                }

            } else {
                return false;
            }

        } else {
            boolean isMarketDataUpdated = updateMarketData(marketData);

            if (isMarketDataUpdated) {
                boolean consolidatedMarketDataExists = marketDataRepository.existsBySymbolAndSource(marketData.getSymbol(), "CONSOLIDATED");

                if (!consolidatedMarketDataExists) {
                    return false;
                } else {
                     return updateConsolidatedMarketData(marketData);
                }

            } else {
                return false;
            }
        }
    }

    @Override
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
    public boolean deleteMarketData(String symbol, String source) {
        if (symbol == null || source == null) {
            return false;
        }

        boolean marketDataExists = marketDataRepository.existsBySymbolAndSource(symbol, source);

        if (marketDataExists) {
            boolean marketDataDeleted = deleteMarketDataRecord(symbol, source);

            if (marketDataDeleted) {
                boolean noMarketDataExists = marketDataRepository.findAllBySymbolAndSourceIsNotIn(symbol, Collections.singletonList("CONSOLIDATED")).isEmpty();

                if (noMarketDataExists) {
                    boolean consolidatedMarketDataExists = marketDataRepository.existsBySymbolAndSource(symbol, "CONSOLIDATED");

                    if (consolidatedMarketDataExists) {
                        boolean consolidatedMarketDataDeleted = deleteConsolidatedMarketData(symbol);

                        if (consolidatedMarketDataDeleted) {
                            boolean dependantMarketDataExists = marketDataRepository.existsByDependsOnSymbol(symbol);

                            if (dependantMarketDataExists) {
                                return updateDependantMarketData(symbol);
                            } else {
                                return true;
                            }

                        } else {
                            return false;
                        }

                    } else {
                        return false;
                    }

                } else {
                    return reConsolidateMarketData(symbol);
                }

            } else {
                return false;
            }

        } else {
            return false;
        }

    }

    private boolean insertMarketData(MarketDataRequestBody marketData) {
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

        return savedMarketData != null;
    }

    private boolean updateMarketData(MarketDataRequestBody marketData) {
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

        return savedMarketData != null;
    }

    private boolean insertConsolidatedMarketData(MarketDataRequestBody marketData) {
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

        return savedConsolidatedMarketData != null;
    }

    private boolean updateConsolidatedMarketData(MarketDataRequestBody marketData) {
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

        return savedConsolidatedMarketData != null;
    }

    private boolean updateDependantMarketData(String symbol) {
        boolean dependantMarketDataExists = marketDataRepository.existsByDependsOnSymbol(symbol);

        if (!dependantMarketDataExists) {
            return false;
        }

        List<MarketData> dependantMarketDataList = marketDataRepository.findAllByDependsOnSymbol(symbol);

        if (dependantMarketDataList == null || dependantMarketDataList.isEmpty()) {
            return false;
        }

        for (MarketData dependantMarketData : dependantMarketDataList) {
            dependantMarketData.setDependsOnSymbol(null);

            double theoreticalPrice = calculateTheoreticalPrice(dependantMarketData.getDependsOnSymbol(), dependantMarketData.getVolatility());

            dependantMarketData.setTheoreticalPrice(theoreticalPrice);

            marketDataRepository.save(dependantMarketData);

            MarketData savedDependantMarketData = marketDataRepository.findBySymbolAndSource(dependantMarketData.getSymbol(), dependantMarketData.getSource());

            if (savedDependantMarketData.getDependsOnSymbol() != null) {
                return false;
            }
        }

        return true;
    }

    private boolean deleteMarketDataRecord(String symbol, String source) {
        boolean marketDataExists = marketDataRepository.existsBySymbolAndSource(symbol, source);

        if (!marketDataExists) {
            return false;
        }

        marketDataRepository.deleteBySymbolAndSource(symbol, source);

        return !marketDataRepository.existsBySymbolAndSource(symbol, source);
    }

    private boolean deleteConsolidatedMarketData(String symbol) {
        boolean consolidatedMarketDataExists = marketDataRepository.existsBySymbolAndSource(symbol, "CONSOLIDATED");

        if (!consolidatedMarketDataExists) {
            return false;
        }

        marketDataRepository.deleteBySymbolAndSource(symbol, "CONSOLIDATED");

        return !marketDataRepository.existsBySymbolAndSource(symbol, "CONSOLIDATED");
    }

    private boolean reConsolidateMarketData(String symbol) {
        List<MarketData> marketDataList = marketDataRepository.findAllBySymbolAndSourceIsNotInOrderByMarketTimestampDesc(symbol, Collections.singletonList("CONSOLIDATED"));

        if (marketDataList == null || marketDataList.isEmpty()) {
            return false;
        }

        MarketData existingConsolidatedMarketData = marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED");

        if (existingConsolidatedMarketData == null) {
            return false;
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

        return savedConsolidatedMarketData != null;
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
