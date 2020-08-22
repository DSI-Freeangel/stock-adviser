package com.dsi.adviser.core;

import reactor.core.publisher.Mono;

public interface PriceHistoryAggregator {
    Mono<Void> aggregate(String stockCode);
}