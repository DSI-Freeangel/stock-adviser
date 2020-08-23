package com.dsi.adviser.core;

import com.dsi.adviser.core.model.StockStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockStatisticsSource {
    private final StockStatisticsExtractor stockStatisticsExtractor;
    private final PriceHistoryAggregator priceHistoryAggregator;
    private final ExecutorService extractExecutor = Executors.newSingleThreadExecutor();

    public Mono<StockStatistics> getStatistics(String stockCode){
        return priceHistoryAggregator.aggregate(stockCode)
                .publishOn(Schedulers.fromExecutor(extractExecutor))
                .doOnNext(el -> log.info("Extracting data for stock {}", stockCode))
                .then(stockStatisticsExtractor.extract(stockCode))
                .onErrorResume(this::handleError);
    }

    public Mono<StockStatistics> handleError(Throwable e) {
        if(e instanceof NoPriceDataException) {
            log.error(e.getMessage());
        } else {
            log.error("Error while getting data for stock", e);
        }
        return Mono.empty();
    }
}