package com.dsi.adviser.core;

import com.dsi.adviser.core.model.StockStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockStatisticsSource {
    private final StockStatisticsExtractor stockStatisticsExtractor;
    private final PriceHistoryAggregator priceHistoryAggregator;

    public Mono<StockStatistics> getStatistics(String stockCodeFull){
        return priceHistoryAggregator.aggregate(stockCodeFull)
                .then(stockStatisticsExtractor.extract(stockCodeFull))
                .onErrorResume(this::handleError);
    }

    public Mono<StockStatistics> handleError(Throwable e) {
        log.error("Error while getting data for stock", e);
        return Mono.empty();
    }
}