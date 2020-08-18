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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.aggregator", havingValue = "REACTOR")
public class PriceHistoryAggregatorReactor implements PriceHistoryAggregator {
    private final PriceService priceService;
    private final PriceDataProvider priceDataProvider;

    @Override
    public Mono<Void> aggregate(String stockCodeFull) {
        Mono<LocalDate> lastDate = priceService.findLastAvailablePrice(stockCodeFull, Period.DAY)
                .map(PriceData::getDate)
                .switchIfEmpty(Mono.just(LocalDate.now().minusYears(100)))
                .filter(date -> ChronoUnit.DAYS.between(date, LocalDate.now()) > 30).cache();
        return lastDate
                .flatMapMany(date -> priceDataProvider.getPriceData(stockCodeFull, date))
                .window(1000)
                .flatMap(priceService::saveAll)
                .concatWith(this.completeMonth(stockCodeFull, lastDate))
                .groupBy(priceData -> DateUtils.getFirstDayOfMonth(priceData.getDate()))
                .flatMap(group -> group.reduce(new PriceDataAggregator())
                        .map(priceData -> this.setDateAndPeriod(priceData, group.key(), Period.MONTH)))
                .window(1000)
                .flatMap(priceService::saveAll)
                .concatWith(this.completeQuarter(stockCodeFull, lastDate))
                .groupBy(priceData -> DateUtils.getFirstMonthOfQuarter(priceData.getDate()))
                .flatMap(group -> group.reduce(new PriceDataAggregator())
                        .map(priceData -> this.setDateAndPeriod(priceData, group.key(), Period.QUARTER)))
                .window(1000)
                .flatMap(priceService::saveAll)
                .concatWith(this.completeYear(stockCodeFull, lastDate))
                .groupBy(priceData -> DateUtils.getFirstMonthOfYear(priceData.getDate()))
                .flatMap(group -> group.reduce(new PriceDataAggregator())
                        .map(priceData -> this.setDateAndPeriod(priceData, group.key(), Period.YEAR)))
                .window(1000)
                .flatMap(priceService::saveAll)
                .then();
    }

    private PriceData setDateAndPeriod(PriceData priceData, LocalDate date, Period type) {
        PriceModel.PriceModelBuilder builder = PriceModel.builder();
        BeanUtils.copyProperties(priceData, builder);
        return builder.setType(type)
                .setDate(date)
                .build();
    }

    private Flux<PriceData> completeYear(String stockCodeFull, Mono<LocalDate> lastDate) {
        return lastDate
                .flatMapMany(date -> priceService.findPricesForInterval(stockCodeFull, Period.QUARTER, DateUtils.getFirstMonthOfYear(date), DateUtils.getFirstMonthOfQuarter(date).minusDays(1)));
    }

    private Flux<PriceData> completeQuarter(String stockCodeFull, Mono<LocalDate> lastDate) {
        return lastDate
                .flatMapMany(date -> priceService.findPricesForInterval(stockCodeFull, Period.MONTH, DateUtils.getFirstMonthOfQuarter(date), DateUtils.getFirstDayOfMonth(date).minusDays(1)));
    }

    private Flux<PriceData> completeMonth(String stockCodeFull, Mono<LocalDate> lastDate) {
        return lastDate.flatMapMany(date -> priceService.findPricesForInterval(stockCodeFull, Period.DAY, DateUtils.getFirstDayOfMonth(date), date));
    }
}
