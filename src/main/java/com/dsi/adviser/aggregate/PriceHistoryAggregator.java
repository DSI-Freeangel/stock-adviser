package com.dsi.adviser.aggregate;

import com.dsi.adviser.integration.priceData.PriceDataProvider;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PriceHistoryAggregator {
    private final PriceService priceService;
    private final PriceDataProvider priceDataProvider;

    public Mono<PriceStatistics> aggregatePrices(String stockCodeFull){
        return aggregate(stockCodeFull)
                .then(load(stockCodeFull));
    }

    private Mono<Void> aggregate(String stockCodeFull) {
        Mono<LocalDate> lastDate = priceService.findLastAvailablePrice(stockCodeFull, Period.DAY)
                .map(PriceData::getDate)
                .switchIfEmpty(Mono.just(LocalDate.now().minusYears(100)))
                .filter(date -> ChronoUnit.DAYS.between(date, LocalDate.now()) > 30).cache();
        return lastDate
                .flatMapMany(date -> priceDataProvider.getPriceData(stockCodeFull, date))
                .window(1000)
                .flatMap(priceService::saveAll)
                .concatWith(priceService.completeMonth(lastDate))
                .groupBy(this::getFirstDayOfMonth)
                .flatMap(group -> group.reduce(new PriceDataAggregator(group.key(), Period.MONTH)))
                .window(1000)
                .flatMap(priceService::saveAll)
                .concatWith(priceService.completeQuarter(lastDate))
                .groupBy(this::getFirstMonthOfQuarter)
                .flatMap(group -> group.reduce(new PriceDataAggregator(group.key(), Period.QUARTER)))
                .window(1000)
                .flatMap(priceService::saveAll)
                .concatWith(priceService.completeYear(lastDate))
                .groupBy(this::getFirstMonthOfYear)
                .flatMap(group -> group.reduce(new PriceDataAggregator(group.key(), Period.QUARTER)))
                .window(1000)
                .flatMap(priceService::saveAll)
                .then();
    }

    private LocalDate getFirstMonthOfYear(PriceData priceData) {
        return priceData.getDate().withMonth(1);
    }

    private LocalDate getFirstDayOfMonth(PriceData priceData) {
        return priceData.getDate().withDayOfMonth(1);
    }

    private LocalDate getFirstMonthOfQuarter(PriceData priceData) {
        int month = (priceData.getDate().getMonthValue() / 3 + 1) * 3 - 2;
        return priceData.getDate().withMonth(month);
    }

    private Mono<PriceStatistics> load(String stockCodeFull) {
        //TODO: implement
        return Mono.just(PriceStatisticsModel.builder().build());
    }
}
