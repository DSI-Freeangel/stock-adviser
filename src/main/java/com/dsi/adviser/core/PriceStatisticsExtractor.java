package com.dsi.adviser.core;

import com.dsi.adviser.price.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PriceStatisticsExtractor {
    private final PriceService priceService;

    public Mono<PriceStatistics> extract(String stockCodeFull) {
        //TODO: implement
        return Mono.just(PriceStatisticsModel.builder().build());
    }
}