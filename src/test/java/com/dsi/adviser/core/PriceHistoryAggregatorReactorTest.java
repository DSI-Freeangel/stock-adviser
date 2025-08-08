package com.dsi.adviser.core;

import com.dsi.adviser.integration.priceData.PriceDataProvider;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceModel;
import com.dsi.adviser.price.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PriceHistoryAggregatorReactorTest {
    private static final String STOCK_CODE = "IBM";

    private final PriceService priceService = mock(PriceService.class);
    private final PriceDataProvider priceDataProvider = mock(PriceDataProvider.class);
    private final PriceHistoryAggregatorReactor priceHistoryAggregator = new PriceHistoryAggregatorReactor(priceService, priceDataProvider);

    @BeforeEach
    public void init() {
        when(priceService.saveAll(any())).then(i -> i.getArgument(0, Flux.class));
    }

    @Test
    @Disabled
    public void aggregatePrices() {
        LocalDate lastDate = LocalDate.of(2019, 8, 17);
        LocalDate currentDate = LocalDate.of(2020, 8, 14);
        givenLastAvailableDayDataForDate(lastDate);
        givenPriceDataCanBeLoadedForInterval(lastDate, currentDate);

        priceHistoryAggregator.aggregate(STOCK_CODE).block();
    }

    private void givenLastAvailableDayDataForDate(LocalDate lastDate) {
        PriceModel model = getPriceModelForDateAndType(lastDate, Period.DAY);
        Mono<PriceData> lastPrice = Mono.just(model);
        when(priceService.findLastAvailablePrice(eq(STOCK_CODE), eq(Period.DAY))).thenReturn(lastPrice);
    }

    private void givenPriceDataCanBeLoadedForInterval(LocalDate lastDate, LocalDate currentDate) {
        when(priceDataProvider.getPriceData(eq(STOCK_CODE), eq(lastDate))).thenReturn(Flux.fromIterable(generatePriceData(lastDate, currentDate)));
    }

    private List<PriceData> generatePriceData(LocalDate lastDate, LocalDate currentDate) {
        List<PriceData> result = new ArrayList<>();
        LocalDate cursor = lastDate.plusDays(1);
        while (cursor.isBefore(currentDate)) {
            if(cursor.getDayOfMonth() > 20) {
                cursor = cursor.withDayOfMonth(1).plusMonths(1);
            } else {
                result.add(getPriceModelForDateAndType(currentDate, Period.DAY));
                cursor = cursor.plusDays(1);
            }
        }
        return result;
    }

    private PriceModel getPriceModelForDateAndType(LocalDate date, Period period) {
        return PriceModel.builder()
                .setStockCode(STOCK_CODE)
                .setDate(date)
                .setType(period)
                .setPrice(10.0)
                .setPriceMin(9.0)
                .setPriceMax(11.0)
                .build();
    }
}