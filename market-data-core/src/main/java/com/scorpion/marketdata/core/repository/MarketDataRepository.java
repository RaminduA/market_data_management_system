package com.scorpion.marketdata.core.repository;

import com.scorpion.marketdata.core.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    boolean existsBySymbolAndSource(String symbol, String source);
    boolean existsByDependsOnSymbol(String dependsOnSymbol);
    MarketData findBySymbolAndSource(String symbol, String source);
    List<MarketData> findAllBySymbolAndSourceIsNotIn(String symbol, List<String> sources);
    List<MarketData> findAllBySymbolAndSourceIsNotInOrderByMarketTimestampDesc(String symbol, List<String> sources);
    List<MarketData> findAllByDependsOnSymbol(String dependsOnSymbol);
    void deleteBySymbolAndSource(String symbol, String source);
}
