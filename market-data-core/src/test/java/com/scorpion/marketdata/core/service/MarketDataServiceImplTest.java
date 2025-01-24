package com.scorpion.marketdata.core.service;

import com.scorpion.marketdata.api.dto.MarketDataRequestBody;
import com.scorpion.marketdata.core.dto.MarketDataResponseBody;
import com.scorpion.marketdata.core.dto.TransactionStatusDto;
import com.scorpion.marketdata.core.entity.MarketData;
import com.scorpion.marketdata.core.repository.MarketDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceImplTest {
    @Mock
    private MarketDataRepository marketDataRepository;

    @InjectMocks
    private MarketDataServiceImpl marketDataService;

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveMarketData_WhenDataIsValidAndNew() {
        MarketDataRequestBody marketData = new MarketDataRequestBody(
                "AAPL", 150.0, 149.0, 149.5, 150.5,
                "2025-01-01T10:00:00", "TSLA", "NASDAQ",
                null, 0.0, 0.0);

        // Mock repository behavior
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(false);
        when(marketDataRepository.save(any())).thenReturn(new MarketData());

        // Call the method
        TransactionStatusDto result = marketDataService.saveMarketData(marketData);

        // Assertions
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Market data saved successfully.", result.getMessage());

        // Verify interactions
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).save(any());
    }


    @Test
    void testSaveMarketData_WhenMarketDataAlreadyExists() {
        MarketDataRequestBody marketData = new MarketDataRequestBody("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", "TSLA", "NASDAQ", null, 0.0, 0.0);

        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(true);
        when(marketDataRepository.save(any())).thenReturn(new MarketData());

        TransactionStatusDto result = marketDataService.saveMarketData(marketData);

        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Market data saved successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).save(any());
    }


    @Test
    void testSaveMarketData_WhenMarketDataIsNull() {
        TransactionStatusDto result = marketDataService.saveMarketData(null);

        assertNotNull(result);
        assertFalse(result.getStatus());
        assertEquals("Invalid market data.", result.getMessage());
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testSaveMarketData_NewMarketDataWithExistingConsolidated() {
        MarketDataRequestBody marketData = new MarketDataRequestBody("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", "TSLA", "NASDAQ", null, 0.0, 0.0);

        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(false);
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(true);

        TransactionStatusDto result = marketDataService.saveMarketData(marketData);

        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Market data saved successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "CONSOLIDATED");
    }

    @Test
    void testSaveMarketData_SaveOperationFails() {
        MarketDataRequestBody marketData = new MarketDataRequestBody("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", "TSLA", "NASDAQ", null, 0.0, 0.0);

        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(false);
        doThrow(new RuntimeException("Save failed")).when(marketDataRepository).save(any());

        TransactionStatusDto result = marketDataService.saveMarketData(marketData);

        assertNotNull(result);
        assertFalse(result.getStatus());
        assertEquals("Error saving market data.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).save(any());
    }

    @Test
    void testSaveMarketData_ConsolidatedUpdateFails() {
        MarketDataRequestBody marketData = new MarketDataRequestBody("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", "TSLA", "NASDAQ", null, 0.0, 0.0);

        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(false);
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(true);
        doThrow(new RuntimeException("Consolidated update failed")).when(marketDataRepository).save(any());

        TransactionStatusDto result = marketDataService.saveMarketData(marketData);

        assertNotNull(result);
        assertFalse(result.getStatus());
        assertEquals("Error saving market data.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "CONSOLIDATED");
    }

    @Test
    void testSaveMarketData_WithNullFields() {
        MarketDataRequestBody marketData = new MarketDataRequestBody(null, 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", "TSLA", "NASDAQ", null, 0.0, 0.0);

        TransactionStatusDto result = marketDataService.saveMarketData(marketData);

        assertNotNull(result);
        assertFalse(result.getStatus());
        assertEquals("Invalid market data.", result.getMessage());
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testSaveMarketData_ComplexMarketData() {
        MarketDataRequestBody marketData = new MarketDataRequestBody("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", "TSLA", "NASDAQ", null, 0.0, 0.0);

        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(false);
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(false);

        TransactionStatusDto result = marketDataService.saveMarketData(marketData);

        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Market data saved successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, times(2)).save(any());
    }


    @Test
    void testGetMarketDataSpecific_ValidSymbolAndSource() {
        String symbol = "AAPL";
        String source = "NASDAQ";
        MarketData mockEntity = new MarketData(
                "AAPL", 150.0, 149.0, 150.5, 151.0, "2025-01-24T10:15:30Z",
                "AAPL_DEPENDENT", "NASDAQ", LocalDate.of(2024, 12, 31), 2.5, 0.25, 0.01, 150.3
        );

        when(marketDataRepository.findBySymbolAndSource(symbol, source)).thenReturn(mockEntity);

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(symbol, source);

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        assertEquals("NASDAQ", result.getSource());
        assertEquals(150.0, result.getLastTradedPrice());
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, source);
    }

    @Test
    void testGetMarketDataSpecific_InvalidSymbol() {
        String symbol = "INVALID";
        String source = "NASDAQ";
        when(marketDataRepository.findBySymbolAndSource(symbol, source)).thenReturn(null);

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(symbol, source);

        assertNull(result);
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, source);
    }

    @Test
    void testGetMarketDataSpecific_InvalidSource() {
        String symbol = "AAPL";
        String source = "INVALID_SOURCE";
        when(marketDataRepository.findBySymbolAndSource(symbol, source)).thenReturn(null);

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(symbol, source);

        assertNull(result);
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, source);
    }

    @Test
    void testGetMarketDataSpecific_NullSymbol() {
        String source = "NASDAQ";

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(null, source);

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataSpecific_NullSource() {
        String symbol = "AAPL";

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(symbol, null);

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataSpecific_NullSymbolAndSource() {
        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(null, null);

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataSpecific_EmptySymbol() {
        String source = "NASDAQ";

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific("", source);

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataSpecific_EmptySource() {
        String symbol = "AAPL";

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(symbol, "");

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataSpecific_RepositoryThrowsException() {
        String symbol = "AAPL";
        String source = "NASDAQ";
        when(marketDataRepository.findBySymbolAndSource(symbol, source))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> marketDataService.getMarketDataSpecific(symbol, source));

        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, source);
    }

    @Test
    void testGetMarketDataSpecific_SymbolWithSpecialCharacters() {
        String symbol = "BRK.A";
        String source = "NYSE";
        MarketData mockEntity = new MarketData(
                "BRK.A", 500000.0, 499999.0, 500001.0, 500002.0, "2025-01-24T10:15:30Z",
                "BRK_DEPENDENT", "NYSE", LocalDate.of(2024, 12, 31), 2.5, 0.5, 0.05, 500001.5
        );

        when(marketDataRepository.findBySymbolAndSource(symbol, source)).thenReturn(mockEntity);

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific(symbol, source);

        assertNotNull(result);
        assertEquals("BRK.A", result.getSymbol());
        assertEquals("NYSE", result.getSource());
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, source);
    }


    @Test
    void testGetMarketDataSpecific_WhenDataExists() {
        MarketData marketData = new MarketData("AAPL", 2800.0, 2799.0, 2799.5, 2801.0, "2025-01-01T11:00:00", null, "NASDAQ", null, 0.0, 0.0, 0.0, 0.0);


        when(marketDataRepository.findBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(marketData);

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific("AAPL", "NASDAQ");

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        assertEquals("NASDAQ", result.getSource());
        verify(marketDataRepository, times(1)).findBySymbolAndSource("AAPL", "NASDAQ");
    }

    @Test
    void testGetMarketDataSpecific_WhenDataDoesNotExist() {
        when(marketDataRepository.findBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(null);

        MarketDataResponseBody result = marketDataService.getMarketDataSpecific("AAPL", "NASDAQ");

        assertNull(result);
        verify(marketDataRepository, times(1)).findBySymbolAndSource("AAPL", "NASDAQ");
    }






    @Test
    void testGetMarketDataConsolidated_ValidSymbol() {
        String symbol = "AAPL";
        MarketData mockEntity = new MarketData(
                "AAPL", 150.0, 149.0, 150.5, 151.0, "2025-01-24T10:15:30Z",
                "AAPL_DEPENDENT", "CONSOLIDATED", LocalDate.of(2024, 12, 31), 2.5, 0.25, 0.01, 150.3
        );

        when(marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED")).thenReturn(mockEntity);

        MarketDataResponseBody result = marketDataService.getMarketDataConsolidated(symbol);

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        assertEquals(150.0, result.getLastTradedPrice());
        assertEquals(149.0, result.getBidPrice());
        assertEquals("2024-12-31", result.getLastCouponDate());
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, "CONSOLIDATED");
    }


    @Test
    void testGetMarketDataConsolidated_InvalidSymbol() {
        String symbol = "INVALID";
        when(marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED")).thenReturn(null);

        MarketDataResponseBody result = marketDataService.getMarketDataConsolidated(symbol);

        assertNull(result);
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataConsolidated_NullSymbol() {
        MarketDataResponseBody result = marketDataService.getMarketDataConsolidated(null);

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataConsolidated_EmptySymbol() {
        MarketDataResponseBody result = marketDataService.getMarketDataConsolidated("");

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataConsolidated_NotFound() {
        String symbol = "MSFT";
        when(marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED")).thenReturn(null);

        MarketDataResponseBody result = marketDataService.getMarketDataConsolidated(symbol);

        assertNull(result);
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataConsolidated_RepositoryThrowsException() {
        String symbol = "AAPL";
        when(marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED"))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> marketDataService.getMarketDataConsolidated(symbol));

        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataConsolidated_CaseInsensitive() {
        String symbol = "aapl";
        MarketData mockEntity = new MarketData(
                "AAPL", 150.0, 149.0, 150.5, 151.0, "2025-01-24T10:15:30Z",
                "AAPL_DEPENDENT", "CONSOLIDATED", LocalDate.of(2024, 12, 31), 2.5, 0.25, 0.01, 150.3
        );
        when(marketDataRepository.findBySymbolAndSource(symbol.toUpperCase(), "CONSOLIDATED")).thenReturn(mockEntity);

        MarketDataResponseBody result = marketDataService.getMarketDataConsolidated(symbol.toUpperCase());

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol.toUpperCase(), "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataConsolidated_SpecialCharacters() {
        String symbol = "BRK.A";
        MarketData mockEntity = new MarketData(
                "BRK.A", 500000.0, 499999.0, 500001.0, 500002.0, "2025-01-24T10:15:30Z",
                "BRK_DEPENDENT", "CONSOLIDATED", LocalDate.of(2024, 12, 31), 2.5, 0.5, 0.05, 500001.5
        );

        when(marketDataRepository.findBySymbolAndSource(symbol, "CONSOLIDATED")).thenReturn(mockEntity);

        MarketDataResponseBody result = marketDataService.getMarketDataConsolidated(symbol);

        assertNotNull(result);
        assertEquals("BRK.A", result.getSymbol());
        verify(marketDataRepository, times(1)).findBySymbolAndSource(symbol, "CONSOLIDATED");
    }




    @Test
    void testGetMarketDataBatch_WithValidSymbols() {
        List<String> symbols = List.of("AAPL", "GOOG");
        MarketData marketData1 = new MarketData("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", null, "CONSOLIDATED", null, 0.0, 0.0, 0.0, 0.0);
        MarketData marketData2 = new MarketData("GOOGL", 2800.0, 2799.0, 2799.5, 2801.0, "2025-01-01T11:00:00", null, "CONSOLIDATED", null, 0.0, 0.0, 0.0, 0.0);

        when(marketDataRepository.findBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(marketData1);
        when(marketDataRepository.findBySymbolAndSource("GOOG", "CONSOLIDATED")).thenReturn(marketData2);

        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(symbols);

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("GOOG", result.get(1).getSymbol());
        verify(marketDataRepository, times(1)).findBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, times(1)).findBySymbolAndSource("GOOG", "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataBatch_WithInvalidSymbols() {
        List<String> symbols = List.of("AAPL", "GOOG");
        when(marketDataRepository.findBySymbolAndSource(anyString(), eq("CONSOLIDATED"))).thenReturn(null);

        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(symbols);

        assertEquals(2, result.size());
        assertNull(result.get(0));
        assertNull(result.get(1));
        verify(marketDataRepository, times(1)).findBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, times(1)).findBySymbolAndSource("GOOG", "CONSOLIDATED");
    }



    @Test
    void testGetMarketDataBatch_WhenSymbolsAreNull() {
        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(null);

        assertNull(result);
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataBatch_WhenSymbolsListIsEmpty() {
        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testGetMarketDataBatch_WhenAllSymbolsReturnValidData() {
        List<String> symbols = List.of("AAPL", "GOOGL");
        MarketData appleData = new MarketData("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", null, "CONSOLIDATED", null, 0.0, 0.0, 0.0, 0.0);
        MarketData googleData = new MarketData("GOOGL", 2800.0, 2799.0, 2799.5, 2801.0, "2025-01-01T11:00:00", null, "CONSOLIDATED", null, 0.0, 0.0, 0.0, 0.0);

        when(marketDataRepository.findBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(appleData);
        when(marketDataRepository.findBySymbolAndSource("GOOGL", "CONSOLIDATED")).thenReturn(googleData);

        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(symbols);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("GOOGL", result.get(1).getSymbol());

        verify(marketDataRepository, times(1)).findBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, times(1)).findBySymbolAndSource("GOOGL", "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataBatch_WhenSomeSymbolsReturnNull() {
        List<String> symbols = List.of("AAPL", "INVALID");
        MarketData appleData = new MarketData("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", null, "CONSOLIDATED", null, 0.0, 0.0, 0.0, 0.0);

        when(marketDataRepository.findBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(appleData);
        when(marketDataRepository.findBySymbolAndSource("INVALID", "CONSOLIDATED")).thenReturn(null);

        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(symbols);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertNull(result.get(1));

        verify(marketDataRepository, times(1)).findBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, times(1)).findBySymbolAndSource("INVALID", "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataBatch_WhenAllSymbolsReturnNull() {
        List<String> symbols = List.of("INVALID1", "INVALID2");

        when(marketDataRepository.findBySymbolAndSource(anyString(), eq("CONSOLIDATED"))).thenReturn(null);

        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(symbols);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertNull(result.get(0));
        assertNull(result.get(1));

        verify(marketDataRepository, times(1)).findBySymbolAndSource("INVALID1", "CONSOLIDATED");
        verify(marketDataRepository, times(1)).findBySymbolAndSource("INVALID2", "CONSOLIDATED");
    }

    @Test
    void testGetMarketDataBatch_WhenDuplicateSymbolsAreProvided() {
        List<String> symbols = List.of("AAPL", "AAPL");
        MarketData appleData = new MarketData("AAPL", 150.0, 149.0, 149.5, 150.5, "2025-01-01T10:00:00", null, "CONSOLIDATED", null, 0.0, 0.0, 0.0, 0.0);

        when(marketDataRepository.findBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(appleData);

        List<MarketDataResponseBody> result = marketDataService.getMarketDataBatch(symbols);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
        assertEquals("AAPL", result.get(1).getSymbol());

        verify(marketDataRepository, times(2)).findBySymbolAndSource("AAPL", "CONSOLIDATED");
    }





    @Test
    void testDeleteMarketData_WhenDataExists() {
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(true);
        when(marketDataRepository.findAllBySymbolAndSourceIsNotIn(eq("AAPL"), anyList())).thenReturn(Collections.emptyList());
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(true);

        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", "NASDAQ");

        assertTrue(result.getStatus());
        assertEquals("Market data deleted successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).findAllBySymbolAndSourceIsNotIn(eq("AAPL"), anyList());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "CONSOLIDATED");
    }

    @Test
    void testDeleteMarketData_WhenDataDoesNotExist() {
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(false);

        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", "NASDAQ");

        assertFalse(result.getStatus());
        assertEquals("Market data does not exist.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, never()).findAllBySymbolAndSourceIsNotIn(eq("AAPL"), anyList());
    }




    @Test
    void testDeleteMarketData_WhenSymbolIsNull() {
        TransactionStatusDto result = marketDataService.deleteMarketData(null, "NASDAQ");

        assertFalse(result.getStatus());
        assertEquals("Invalid symbol or source.", result.getMessage());
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testDeleteMarketData_WhenSourceIsNull() {
        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", null);

        assertFalse(result.getStatus());
        assertEquals("Invalid symbol or source.", result.getMessage());
        verifyNoInteractions(marketDataRepository);
    }

    @Test
    void testDeleteMarketData_WhenNoOtherMarketDataExists() {
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(true);
        when(marketDataRepository.findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED")))
                .thenReturn(Collections.emptyList());
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(true);
        when(marketDataRepository.existsByDependsOnSymbol("AAPL")).thenReturn(false);

        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", "NASDAQ");

        assertTrue(result.getStatus());
        assertEquals("Market data deleted successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED"));
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, times(1)).existsByDependsOnSymbol("AAPL");
    }

    @Test
    void testDeleteMarketData_WhenDependentDataExists() {
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(true);
        when(marketDataRepository.findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED")))
                .thenReturn(Collections.emptyList());
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "CONSOLIDATED")).thenReturn(true);
        when(marketDataRepository.existsByDependsOnSymbol("AAPL")).thenReturn(true);

        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", "NASDAQ");

        assertTrue(result.getStatus());
        assertEquals("Market data deleted successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED"));
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, times(1)).existsByDependsOnSymbol("AAPL");
    }

    @Test
    void testDeleteMarketData_WhenReConsolidationIsRequired() {
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(true);
        when(marketDataRepository.findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED")))
                .thenReturn(List.of(new MarketData(
                        "AAPL", 150.0, 149.0, 150.5, 151.0, "2025-01-24T10:15:30Z",
                        "AAPL_DEPENDENT", "CONSOLIDATED", LocalDate.of(2024, 12, 31), 2.5, 0.25, 0.01, 150.3
                )));

        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", "NASDAQ");

        assertTrue(result.getStatus());
        assertEquals("Market data deleted successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED"));
        verify(marketDataRepository, never()).existsBySymbolAndSource("AAPL", "CONSOLIDATED");
        verify(marketDataRepository, never()).existsByDependsOnSymbol("AAPL");
    }

    @Test
    void testDeleteMarketData_WhenNoMarketDataExists() {
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(false);

        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", "NASDAQ");

        assertFalse(result.getStatus());
        assertEquals("Market data does not exist.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, never()).findAllBySymbolAndSourceIsNotIn(eq("AAPL"), anyList());
    }

    @Test
    void testDeleteMarketData_WithMultipleExistingSources() {
        when(marketDataRepository.existsBySymbolAndSource("AAPL", "NASDAQ")).thenReturn(true);
        when(marketDataRepository.findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED")))
                .thenReturn(List.of(new MarketData(
                        "AAPL", 150.0, 149.0, 150.5, 151.0, "2025-01-24T10:15:30Z",
                        "NYSE", "CONSOLIDATED", LocalDate.of(2024, 12, 31), 2.5, 0.25, 0.01, 150.3
                ), new MarketData(
                        "AAPL", 150.0, 149.0, 150.5, 151.0, "2025-01-24T10:15:30Z",
                        "LSE", "CONSOLIDATED", LocalDate.of(2024, 12, 31), 2.5, 0.25, 0.01, 150.3
                )));

        TransactionStatusDto result = marketDataService.deleteMarketData("AAPL", "NASDAQ");

        assertTrue(result.getStatus());
        assertEquals("Market data deleted successfully.", result.getMessage());
        verify(marketDataRepository, times(1)).existsBySymbolAndSource("AAPL", "NASDAQ");
        verify(marketDataRepository, times(1)).findAllBySymbolAndSourceIsNotIn("AAPL", Collections.singletonList("CONSOLIDATED"));
    }

}
