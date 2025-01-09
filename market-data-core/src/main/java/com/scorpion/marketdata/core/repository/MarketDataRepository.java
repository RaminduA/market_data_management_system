package com.scorpion.marketdata.core.repository;

import com.scorpion.marketdata.core.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    boolean existsBySymbolAndSource(String symbol, String source);
    boolean existsByDependsOnSymbol(String dependsOnSymbol);
    MarketData findBySymbolAndSource(String symbol, String source);
    List<MarketData> findAllBySymbolAndSourceIsNotIn(String symbol, List<String> sources);
    List<MarketData> findAllBySymbolAndSourceIsNotInOrderByMarketTimestampDesc(String symbol, List<String> sources);
    List<MarketData> findAllByDependsOnSymbol(String dependsOnSymbol);
    void deleteBySymbolAndSource(String symbol, String source);
}
