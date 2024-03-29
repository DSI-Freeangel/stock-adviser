package com.dsi.adviser.core;

import com.dsi.adviser.integration.priceData.PriceDataProvider;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceModel;
import com.dsi.adviser.price.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.aggregator", havingValue = "REACTOR")
public class PriceHistoryAggregatorReactor implements PriceHistoryAggregator {
    private final PriceService priceService;
    private final PriceDataProvider priceDataProvider;

    @Override
    public Mono<Void> aggregate(String stockCode) {
        Mono<LocalDate> lastDate = priceService.findLastAvailablePrice(stockCode, Period.DAY)
                .map(PriceData::getDate)
                .switchIfEmpty(Mono.just(LocalDate.now().minusYears(100)))
                .filter(date -> ChronoUnit.DAYS.between(date, LocalDate.now()) > 30).cache();
        return lastDate
                .flatMapMany(date -> priceDataProvider.getPriceData(stockCode, date))
                .windowTimeout(1000, Duration.ofSeconds(1))
                .flatMap(priceService::saveAll,1, 1)
                .concatWith(this.completeMonth(stockCode, lastDate))
                .groupBy(priceData -> DateUtils.getFirstDayOfMonth(priceData.getDate()))
                .flatMap(group -> group.reduce(new PriceDataAggregator())
                        .map(priceData -> this.setDateAndPeriod(priceData, group.key(), Period.MONTH)))
                .windowTimeout(1000, Duration.ofSeconds(1))
                .flatMap(priceService::saveAll,1, 1)
                .concatWith(this.completeQuarter(stockCode, lastDate))
                .groupBy(priceData -> DateUtils.getFirstMonthOfQuarter(priceData.getDate()))
                .flatMap(group -> group.reduce(new PriceDataAggregator())
                        .map(priceData -> this.setDateAndPeriod(priceData, group.key(), Period.QUARTER)))
                .windowTimeout(1000, Duration.ofSeconds(1))
                .flatMap(priceService::saveAll,1, 1)
                .concatWith(this.completeYear(stockCode, lastDate))
                .groupBy(priceData -> DateUtils.getFirstMonthOfYear(priceData.getDate()))
                .flatMap(group -> group.reduce(new PriceDataAggregator())
                        .map(priceData -> this.setDateAndPeriod(priceData, group.key(), Period.YEAR)))
                .windowTimeout(1000, Duration.ofSeconds(1))
                .flatMap(priceService::saveAll,1, 1)
                .then();
    }

    private PriceData setDateAndPeriod(PriceData priceData, LocalDate date, Period type) {
        PriceModel.PriceModelBuilder builder = PriceModel.builder();
        BeanUtils.copyProperties(priceData, builder);
        return builder.setType(type)
                .setDate(date)
                .build();
    }

    private Flux<PriceData> completeYear(String stockCode, Mono<LocalDate> lastDate) {
        return lastDate
                .flatMapMany(date -> priceService.findPricesForInterval(stockCode, Period.QUARTER, DateUtils.getFirstMonthOfYear(date), DateUtils.getFirstMonthOfQuarter(date).minusDays(1)));
    }

    private Flux<PriceData> completeQuarter(String stockCode, Mono<LocalDate> lastDate) {
        return lastDate
                .flatMapMany(date -> priceService.findPricesForInterval(stockCode, Period.MONTH, DateUtils.getFirstMonthOfQuarter(date), DateUtils.getFirstDayOfMonth(date).minusDays(1)));
    }

    private Flux<PriceData> completeMonth(String stockCode, Mono<LocalDate> lastDate) {
        return lastDate.flatMapMany(date -> priceService.findPricesForInterval(stockCode, Period.DAY, DateUtils.getFirstDayOfMonth(date), date));
    }
}
