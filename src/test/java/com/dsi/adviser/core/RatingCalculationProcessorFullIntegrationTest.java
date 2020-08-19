package com.dsi.adviser.core;

import com.dsi.adviser.financial.FinancialRepository;
import com.dsi.adviser.integration.financialData.FinancialDataRepository;
import com.dsi.adviser.integration.priceData.PriceDataRepository;
import com.dsi.adviser.price.PriceRepository;
import com.dsi.adviser.rating.Rating;
import com.dsi.adviser.rating.RatingRepository;
import com.dsi.adviser.stock.StockEntity;
import com.dsi.adviser.stock.StockRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
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

    @Before
    public void setUp() {
        stockRepository.save(StockEntity.builder().setStockCodeFull("NYSE:IBM").setCode("IBM").setExchange("NYSE").build()).block();
        stockRepository.save(StockEntity.builder().setStockCodeFull("NASDAQ:TSLA").setCode("TSLA").setExchange("NASDAQ").build()).block();
    }

    @After
    public void tearDown() {
        ratingRepository.deleteAll().block();
        System.out.println("RatingRepository is clear!!!");
        stockRepository.deleteAll().block();
        System.out.println("StockRepository is clear!!!");
        financialRepository.deleteAll().block();
        System.out.println("FinancialRepository is clear!!!");
        priceRepository.deleteAll().block();
        System.out.println("PriceRepository is clear!!!");
        priceDataRepository.deleteAll().block();
        System.out.println("PriceDataRepository is clear!!!");
        financialDataRepository.deleteAll().block();
        System.out.println("FinancialDataRepository is clear!!!");
    }

    @Test
    public void updateRating() {
        Flux<Rating> rating = ratingCalculationProcessor.updateRating();

        StepVerifier.create(rating)
                .expectNextCount(2)
                .verifyComplete();
    }
}