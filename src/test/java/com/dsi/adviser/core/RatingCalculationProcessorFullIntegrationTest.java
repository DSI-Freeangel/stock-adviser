package com.dsi.adviser.core;

import com.dsi.adviser.financial.FinancialRepository;
import com.dsi.adviser.integration.financialData.FinancialDataRepository;
import com.dsi.adviser.integration.priceData.PriceDataRepository;
import com.dsi.adviser.price.PriceRepository;
import com.dsi.adviser.rating.Rating;
import com.dsi.adviser.rating.RatingRepository;
import com.dsi.adviser.stock.StockEntity;
import com.dsi.adviser.stock.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
@SpringJUnitConfig
public class RatingCalculationProcessorFullIntegrationTest {

    @Autowired
    private RatingCalculationProcessor ratingCalculationProcessor;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private FinancialRepository financialRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private PriceDataRepository priceDataRepository;

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @BeforeEach
    public void setUp() {
        stockRepository.save(StockEntity.builder().setStockCode("IBM").build()).block();
        stockRepository.save(StockEntity.builder().setStockCode("FB").build()).block();
        stockRepository.save(StockEntity.builder().setStockCode("GOOG").build()).block();
        stockRepository.save(StockEntity.builder().setStockCode("NVDA").build()).block();
        stockRepository.save(StockEntity.builder().setStockCode("AMZN").build()).block();
        stockRepository.save(StockEntity.builder().setStockCode("AAPL").build()).block();
        stockRepository.save(StockEntity.builder().setStockCode("TSLA").build()).block();
    }

    @AfterEach
    public void tearDown() {
//        ratingRepository.deleteAll().block();
        System.out.println("RatingRepository is clear!!!");
        stockRepository.deleteAll().block();
        System.out.println("StockRepository is clear!!!");
        financialRepository.deleteAll().block();
        System.out.println("FinancialRepository is clear!!!");
        priceRepository.deleteAll().block();
        System.out.println("PriceRepository is clear!!!");
//        priceDataRepository.deleteAll().block();
        System.out.println("PriceDataRepository is clear!!!");
//        financialDataRepository.deleteAll().block();
        System.out.println("FinancialDataRepository is clear!!!");
    }

    @Test
    @Disabled
    public void updateRating() {
        Flux<Rating> rating = ratingCalculationProcessor.updateRating();

        StepVerifier.create(rating)
                .expectNextCount(7)
                .verifyComplete();
    }
}