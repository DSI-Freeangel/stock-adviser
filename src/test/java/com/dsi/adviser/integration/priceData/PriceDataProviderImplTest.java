package com.dsi.adviser.integration.priceData;

import com.dsi.adviser.integration.client.PriceDataItem;
import com.dsi.adviser.integration.client.PriceDataModel;
import com.dsi.adviser.integration.client.PriceHistorySource;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PriceDataProviderImplTest {
    private static final String STOCK_CODE = "IBM";
    private final PriceHistorySource priceHistorySource = mock(PriceHistorySource.class);
    private final PriceDataRepository priceDataRepository = mock(PriceDataRepository.class);
    private final PriceDataProvider priceDataProvider = new PriceDataProviderImpl(priceDataRepository, priceHistorySource);

    @BeforeEach
    public void init() {
        when(priceDataRepository.insertPriceDataEntities(any())).thenReturn(Mono.empty());
    }

    @Test
    public void getPriceDataWithoutDataUpdateAndFromDateProvided() {
        givenLastStoredPriceDataIsTwoDaysOld();
        givenSomeCountOfRecordsPresentInDBAfterDate(300, LocalDate.now().minusDays(2));
        Flux<PriceData> priceData = priceDataProvider.getPriceData(STOCK_CODE, LocalDate.now().minusYears(1));

        AtomicInteger counter = new AtomicInteger();
        StepVerifier.create(priceData)
                .thenConsumeWhile(data -> isValidPriceData(data, counter))
                .verifyComplete();
        assertEquals(300, counter.get());
    }

    @Test
    public void getPriceDataWithoutDataUpdateWithoutFromDate() {
        givenLastStoredPriceDataIsTwoDaysOld();
        givenSomeCountOfRecordsPresentInDB(600, LocalDate.now().minusDays(2));
        Flux<PriceData> priceData = priceDataProvider.getPriceData(STOCK_CODE, null);

        AtomicInteger counter = new AtomicInteger();
        StepVerifier.create(priceData)
                .thenConsumeWhile(data -> isValidPriceData(data, counter))
                .verifyComplete();
        assertEquals(600, counter.get());
    }

    @Test
    public void getPriceDataWithPartialUpdateFromDateProvided() {
        givenLastStoredPriceDataIsOneMonthOld();
        givenSomeCountOfRecordsPresentInDBAfterDate(300, LocalDate.now().minusDays(31));
        givenPriceHistorySourceIsAbleToReturnRecords(100, LocalDate.now().minusDays(1));
        Flux<PriceData> priceData = priceDataProvider.getPriceData(STOCK_CODE, LocalDate.now().minusYears(1));

        AtomicInteger counter = new AtomicInteger();
        StepVerifier.create(priceData)
                .thenConsumeWhile(data -> isValidPriceData(data, counter))
                .verifyComplete();
        assertEquals(300, counter.get());
    }

    @Test
    public void getPriceDataWithPartialUpdateWithoutFromDate() {
        givenLastStoredPriceDataIsOneMonthOld();
        givenSomeCountOfRecordsPresentInDB(600, LocalDate.now().minusDays(31));
        givenPriceHistorySourceIsAbleToReturnRecords(100, LocalDate.now().minusDays(1));
        Flux<PriceData> priceData = priceDataProvider.getPriceData(STOCK_CODE, null);

        AtomicInteger counter = new AtomicInteger();
        StepVerifier.create(priceData)
                .thenConsumeWhile(data -> isValidPriceData(data, counter))
                .verifyComplete();
        assertEquals(600, counter.get());
    }

    @Test
    public void getPriceDataWithFullUpdateFromDateProvided() {
        givenNoPriceDataStoredForStock();
        givenSomeCountOfRecordsPresentInDBAfterDate(365, LocalDate.now().minusDays(31));
        givenPriceHistorySourceIsAbleToReturnRecords(1000, LocalDate.now().minusDays(1));
        Flux<PriceData> priceData = priceDataProvider.getPriceData(STOCK_CODE, LocalDate.now().minusYears(1));

        AtomicInteger counter = new AtomicInteger();
        StepVerifier.create(priceData)
                .thenConsumeWhile(data -> isValidPriceData(data, counter))
                .verifyComplete();
        assertEquals(365, counter.get());
    }

    @Test
    public void getPriceDataWithFullUpdateWithoutFromDate() {
        givenNoPriceDataStoredForStock();
        givenSomeCountOfRecordsPresentInDB(1000, LocalDate.now().minusDays(1));
        givenPriceHistorySourceIsAbleToReturnRecords(1000, LocalDate.now().minusDays(1));
        Flux<PriceData> priceData = priceDataProvider.getPriceData(STOCK_CODE, null);

        AtomicInteger counter = new AtomicInteger();
        StepVerifier.create(priceData)
                .thenConsumeWhile(data -> isValidPriceData(data, counter))
                .verifyComplete();
        assertEquals(1000, counter.get());
    }

    private void givenSomeCountOfRecordsPresentInDBAfterDate(int count, LocalDate lastDay) {
        List<PriceDataEntity> results = getPriceDataEntities(count, lastDay);
        when(priceDataRepository.findAllByStockCodeAndDateGreaterThanEqual(eq(STOCK_CODE), any())).thenReturn(Flux.fromIterable(results));
    }

    private void givenSomeCountOfRecordsPresentInDB(int count, LocalDate lastDay) {
        List<PriceDataEntity> results = getPriceDataEntities(count, lastDay);
        when(priceDataRepository.findAllByStockCode(eq(STOCK_CODE))).thenReturn(Flux.fromIterable(results));
    }

    private void givenLastStoredPriceDataIsTwoDaysOld() {
        when(priceDataRepository.findFirstByStockCodeOrderByDateDesc(eq(STOCK_CODE))).thenReturn(Mono.just(buildPriceDataEntity(STOCK_CODE, LocalDate.now().minusDays(2))));
    }

    private void givenLastStoredPriceDataIsOneMonthOld() {
        when(priceDataRepository.findFirstByStockCodeOrderByDateDesc(eq(STOCK_CODE))).thenReturn(Mono.just(buildPriceDataEntity(STOCK_CODE, LocalDate.now().minusDays(31))));
    }

    private void givenNoPriceDataStoredForStock() {
        when(priceDataRepository.findFirstByStockCodeOrderByDateDesc(eq(STOCK_CODE))).thenReturn(Mono.empty());
//        when(priceDataRepository.findAllByStockCodeAndDateGreaterThanEqual(eq(STOCK_CODE), any())).thenReturn(Flux.empty());
//        when(priceDataRepository.findAllByStockCode(eq(STOCK_CODE))).thenReturn(Flux.empty());
    }

    private void givenPriceHistorySourceIsAbleToReturnRecords(int count, LocalDate lastDay) {
        List<PriceDataItem> newData = priceDataItems(count, lastDay);
        when(priceHistorySource.getPriceHistory(eq(STOCK_CODE), any())).thenReturn(Flux.fromIterable(newData));
    }

    private List<PriceDataItem> priceDataItems(int count, LocalDate lastDay) {
        List<PriceDataItem> results = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            results.add(buildPriceDataItem(lastDay.minusDays(i)));
        }
        return results;
    }

    private List<PriceDataEntity> getPriceDataEntities(int count, LocalDate lastDay) {
        List<PriceDataEntity> results = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            results.add(buildPriceDataEntity(STOCK_CODE, lastDay.minusDays(i)));
        }
        return results;
    }

    private PriceDataEntity buildPriceDataEntity(String stockCode, LocalDate date) {
        return PriceDataEntity.builder()
                .setStockCode(stockCode)
                .setDate(date)
                .setPriceClose(20.1)
                .setPriceOpen(19.5)
                .setPriceMin(19.4)
                .setPriceMax(20.3)
                .setVolume(200.0)
                .build();
    }

    private boolean isValidPriceData(PriceData data, AtomicInteger counter) {
        counter.incrementAndGet();
        return data.getStockCode().equals(STOCK_CODE)
                && data.getDate().isBefore(LocalDate.now())
                && data.getPrice().equals(20.1)
                && data.getPriceMin().equals(19.4)
                && data.getPriceMax().equals(20.3)
                && data.getType().equals(Period.DAY);
    }

    private PriceDataItem buildPriceDataItem(LocalDate date) {
        return PriceDataModel.builder()
                .setDate(date)
                .setPriceClose(20.1)
                .setPriceOpen(19.5)
                .setPriceMin(19.4)
                .setPriceMax(20.3)
                .setVolume(200.0)
                .build();
    }
}