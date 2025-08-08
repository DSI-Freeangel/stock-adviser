package com.dsi.adviser.integration.priceData;

import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@SpringJUnitConfig
@ActiveProfiles("test")
public class PriceDataProviderIntegrationTest {
    private static final String STOCK_CODE = "IBM";

    @Autowired
    private PriceDataRepository priceDataRepository;

    @Autowired
    private PriceDataProvider priceDataProvider;

    @AfterEach
    public void init() {
        priceDataRepository.deleteAll().block();
        System.out.println("PriceDataRepository clean!");
    }

    @Test
    public void testAllDataStoredAndReturnedSuccessfully() {
        Flux<PriceData> priceData = priceDataProvider.getPriceData(STOCK_CODE, null);
        AtomicInteger counter = new AtomicInteger();
        StepVerifier.create(priceData)
                .thenConsumeWhile(data -> isValidPriceData(data, counter))
                .verifyComplete();

        Long count = priceDataRepository.count().block();
        assertEquals(counter.get(), count.intValue());
        System.out.println(String.format("%d records saved successfully!!", counter.get()));

        assertTrue(counter.get() > 5000);
    }

    private boolean isValidPriceData(PriceData data, AtomicInteger counter) {
        counter.incrementAndGet();
        return data.getStockCode().equals(STOCK_CODE)
                && (data.getDate().isEqual(LocalDate.now()) || data.getDate().isBefore(LocalDate.now()))
                && data.getPrice() != null
                && data.getPriceMin() != null
                && data.getPriceMax() != null
                && data.getType().equals(Period.DAY);
    }
}
