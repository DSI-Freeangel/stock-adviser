package com.dsi.adviser.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PriceStatisticsSource {
    private final PriceStatisticsExtractor priceStatisticsExtractor;
    private final PriceHistoryAggregator priceHistoryAggregator;

    public Mono<PriceStatistics> aggregatePrices(String stockCodeFull){
        return priceHistoryAggregator.aggregate(stockCodeFull)
                .then(priceStatisticsExtractor.extract(stockCodeFull));
    }
}