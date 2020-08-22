package com.dsi.adviser.core;

import com.dsi.adviser.integration.priceData.PriceDataProvider;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean({PriceHistoryAggregatorReactor.class})
public class PriceHistoryAggregatorDB implements PriceHistoryAggregator {
    private final PriceService priceService;
    private final PriceDataProvider priceDataProvider;
    private final ExecutorService getDataExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService persistExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService continueExecutor = Executors.newFixedThreadPool(2);

    @Override
    public Mono<Void> aggregate(String stockCode) {
        Mono<LocalDate> lastDate = priceService.findLastAvailablePrice(stockCode, Period.DAY)
                .map(PriceData::getDate)
                .switchIfEmpty(Mono.just(LocalDate.now().minusYears(100)))
                .filter(date -> ChronoUnit.DAYS.between(date, LocalDate.now()) > 30).cache();
        return lastDate
                .publishOn(Schedulers.fromExecutor(getDataExecutor))
                .flatMapMany(date -> priceDataProvider.getPriceData(stockCode, date))
                .subscribeOn(Schedulers.fromExecutor(continueExecutor))
                .window(1000)
                .publishOn(Schedulers.fromExecutor(persistExecutor))
                .flatMap(priceService::saveAll, 1, 6)
                .subscribeOn(Schedulers.fromExecutor(continueExecutor))
                .then(priceService.executeAggregationQueries(lastDate, stockCode));
    }
}