package com.dsi.adviser.price;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@DataR2dbcTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class PriceRepositoryTest {
    private static final String STOCK_CODE = "IBM";
    private static final double PRICE = 20.0;
    private static final double PRICE_MIN = 19.0;
    private static final double PRICE_MAX = 21.0;

    @Autowired
    private PriceRepository repository;

    @After
    public void tearDown() {
        repository.deleteAll().block();
        System.out.println("PriceRepository clean!");
    }

    @Test
    public void testYearAggregationPerformed() {
        List<PriceEntity> list = new ArrayList<>();
        list.add(getPriceEntity(STOCK_CODE, LocalDate.of(2020,3,5)));
        list.add(getPriceEntity(STOCK_CODE, LocalDate.of(2020,4,7)));
        list.add(getPriceEntity(STOCK_CODE, LocalDate.of(2020,4,15)));
        list.add(getPriceEntity(STOCK_CODE, LocalDate.of(2020,5,1)));
        list.add(getPriceEntity(STOCK_CODE, LocalDate.of(2020,6,13)));
        list.add(getPriceEntity(STOCK_CODE, LocalDate.of(2020,10,30)));
        repository.saveAll(list).then().block();

        repository.executeMonthAggregationQuery(LocalDate.now().minusYears(1), LocalDate.now().minusYears(1), STOCK_CODE).block();
        repository.executeQuarterAggregationQuery(LocalDate.now().minusYears(1), LocalDate.now().minusYears(1), STOCK_CODE).block();
        repository.executeYearAggregationQuery(LocalDate.now().minusYears(1), STOCK_CODE).block();

        LocalDate allDataPeriod = LocalDate.now().minusYears(100);
        Flux<PriceEntity> monthRecords = repository.findAllByStockCodeAndTypeAndDateGreaterThanEqual(STOCK_CODE, Period.MONTH, allDataPeriod);
        StepVerifier.create(monthRecords)
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.MONTH, LocalDate.of(2020, 3, 1)))
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.MONTH, LocalDate.of(2020, 4, 1)))
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.MONTH, LocalDate.of(2020, 5, 1)))
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.MONTH, LocalDate.of(2020, 6, 1)))
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.MONTH, LocalDate.of(2020, 10, 1)))
                .verifyComplete();

        Flux<PriceEntity> quarterRecords = repository.findAllByStockCodeAndTypeAndDateGreaterThanEqual(STOCK_CODE, Period.QUARTER, allDataPeriod);
        StepVerifier.create(quarterRecords)
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.QUARTER, LocalDate.of(2020, 1, 1)))
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.QUARTER, LocalDate.of(2020, 4, 1)))
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.QUARTER, LocalDate.of(2020, 10, 1)))
                .verifyComplete();

        Flux<PriceEntity> yearRecords = repository.findAllByStockCodeAndTypeAndDateGreaterThanEqual(STOCK_CODE, Period.YEAR, allDataPeriod);
        StepVerifier.create(yearRecords)
                .expectNextMatches(priceEntity -> isValidRecord(priceEntity, STOCK_CODE, Period.YEAR, LocalDate.of(2020, 1, 1)))
                .verifyComplete();
    }

    private boolean isValidRecord(PriceEntity priceEntity, String stockCode, Period period, LocalDate date) {
        return priceEntity.getStockCode().equals(stockCode)
                && priceEntity.getType().equals(period)
                && priceEntity.getDate().equals(date)
                && priceEntity.getPrice().equals(PRICE)
                && priceEntity.getPriceMin().equals(PRICE_MIN)
                && priceEntity.getPriceMax().equals(PRICE_MAX);
    }

    private PriceEntity getPriceEntity(String stockCode, LocalDate date) {
        return PriceEntity.builder()
                .setStockCode(stockCode)
                .setDate(date)
                .setType(Period.DAY)
                .setPrice(PRICE)
                .setPriceMin(PRICE_MIN)
                .setPriceMax(PRICE_MAX)
                .build();
    }

}