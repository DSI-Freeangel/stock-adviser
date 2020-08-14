package com.dsi.adviser.aggregate;

import com.dsi.adviser.integration.priceData.PriceDataProvider;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceModel;
import com.dsi.adviser.price.PriceService;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PriceHistoryAggregatorTest {
    private static final String STOCK_CODE_FULL = "NYSE:IBM";

    private final PriceService priceService = mock(PriceService.class);
    private final PriceDataProvider priceDataProvider = mock(PriceDataProvider.class);
    private final PriceHistoryAggregator priceHistoryAggregator = new PriceHistoryAggregator(priceService, priceDataProvider);

    @Before
    public void init() {
        when(priceService.saveAll(any())).then(i -> i.getArgument(0, Flux.class));
    }

    @Test
    public void aggregatePrices() {
        LocalDate lastDate = LocalDate.of(2019, 8, 17);
        LocalDate currentDate = LocalDate.of(2020, 8, 14);
        givenLastAvailableDayDataForDate(lastDate);
        givenPriceDataCanBeLoadedForInterval(lastDate, currentDate);
    }

    private void givenLastAvailableDayDataForDate(LocalDate lastDate) {
        PriceModel model = getPriceModelForDateAndType(lastDate, Period.DAY);
        Mono<PriceData> lastPrice = Mono.just(model);
        when(priceService.findLastAvailablePrice(eq(STOCK_CODE_FULL), eq(Period.DAY))).thenReturn(lastPrice);
    }

    private void givenPriceDataCanBeLoadedForInterval(LocalDate lastDate, LocalDate currentDate) {
        when(priceDataProvider.getPriceData(eq(STOCK_CODE_FULL), eq(lastDate))).thenReturn(Flux.fromIterable(generatePriceData(lastDate, currentDate)));
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
                .setStockCodeFull(STOCK_CODE_FULL)
                .setDate(date)
                .setType(Period.DAY)
                .setPrice(10.0)
                .setPriceMin(9.0)
                .setPriceMax(11.0)
                .build();
    }
}