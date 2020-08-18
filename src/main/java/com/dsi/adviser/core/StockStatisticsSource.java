package com.dsi.adviser.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StockStatisticsSource {
    private final StockStatisticsExtractor stockStatisticsExtractor;
    private final PriceHistoryAggregator priceHistoryAggregator;

    public Mono<StockStatistics> getStatistics(String stockCodeFull){
        return priceHistoryAggregator.aggregate(stockCodeFull)
                .then(stockStatisticsExtractor.extract(stockCodeFull));
    }
}