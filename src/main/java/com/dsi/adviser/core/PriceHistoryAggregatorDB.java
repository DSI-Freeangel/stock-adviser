package com.dsi.adviser.core;

import com.dsi.adviser.integration.priceData.PriceDataProvider;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean({PriceHistoryAggregatorReactor.class})
public class PriceHistoryAggregatorDB implements PriceHistoryAggregator {
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
                .then(priceService.executeAggregationQueries(lastDate, stockCodeFull));
    }
}