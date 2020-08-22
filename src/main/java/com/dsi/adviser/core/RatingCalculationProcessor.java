package com.dsi.adviser.core;

import com.dsi.adviser.core.model.StockStatistics;
import com.dsi.adviser.rating.Rating;
import com.dsi.adviser.rating.RatingModel;
import com.dsi.adviser.rating.RatingService;
import com.dsi.adviser.stock.Stock;
import com.dsi.adviser.stock.StockService;
import io.vavr.Predicates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingCalculationProcessor {
    private final StockStatisticsSource stockStatisticsSource;
    private final StockService stockService;
    private final RatingService ratingService;
    private final ExecutorService readExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService persistExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService calcualtionExecutor = Executors.newFixedThreadPool(4);


    public Flux<Rating> updateRating() {
        AtomicInteger inputCounter = new AtomicInteger();
        AtomicInteger processingCounter = new AtomicInteger();
        AtomicInteger persistCounter = new AtomicInteger();
        AtomicInteger completeCounter = new AtomicInteger();
        return ratingService.deleteAllByDate(LocalDate.now())
                .thenMany(stockService.findAll())
                .publishOn(Schedulers.fromExecutor(readExecutor))
                .map(Stock::getStockCode)
                .doOnNext(item -> log.info("Going to prepare statistics for #{} {}", inputCounter.incrementAndGet(), item))
                .flatMap(stockStatisticsSource::getStatistics,4, 10)
                .publishOn(Schedulers.fromExecutor(calcualtionExecutor))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(stockStatistics -> log.info("Going to start calculation for #{} {}", processingCounter.incrementAndGet(), stockStatistics.getStockCode()))
                .map(this::prepareCoefficients)
                .collectList()
                .filter(Predicates.not(CollectionUtils::isEmpty))
                .flatMapIterable(this::normalizeRatings)
                .doOnNext(rating -> log.info("Going to save rating for #{} {}", persistCounter.incrementAndGet(), rating.getStockCode()))
                .windowTimeout(1000, Duration.ofSeconds(1))
                .publishOn(Schedulers.fromExecutor(persistExecutor))
                .flatMap(this::persistRatings, 1, 6)
                .doOnNext(rating -> log.info("Rating saved for #{} {}", completeCounter.incrementAndGet(), rating.getStockCode()));
    }

    private void handleError(Throwable throwable, Object source) {
        log.error("Error for element {} : {}", source, throwable);
    }

    private Rating prepareCoefficients(StockStatistics stockStatistics) {
        Double yearlyGrown = (stockStatistics.getPriceLast() - stockStatistics.getPriceYtd()) / stockStatistics.getPriceYtd();
        Double apyGrown = stockStatistics.getDividendsApy() + yearlyGrown;
        Double discount = (stockStatistics.getPriceMaxYtd() - stockStatistics.getPriceLast()) / (stockStatistics.getPriceMaxYtd() - stockStatistics.getPriceMinYtd());
        Double earningValue = stockStatistics.getEnterpriseValue() == 0 ? 0 : stockStatistics.getEarnings() / stockStatistics.getEnterpriseValue();
        Double hyperbolic = calculateHyperbolic(stockStatistics.getYearsPriceAvg());
        return RatingModel.builder()
                .setStockCode(stockStatistics.getStockCode())
                .setApyGrown(apyGrown)
                .setDiscount(discount)
                .setEarningValue(earningValue)
                .setHyperbolic(hyperbolic)
                .build();
    }

    private Double calculateHyperbolic(List<Double> yearsPriceAvg) {
        List<Double> grownList = new ArrayList<>();
        for(int i = 1; i < yearsPriceAvg.size(); i++) {
            Double previous = yearsPriceAvg.get(i - 1);
            Double next = yearsPriceAvg.get(i);
            Double grown = (next - previous) / previous;
            grownList.add(grown);
        }
        Iterator<Double> iterator = grownList.iterator();
        Double total = 0.0;
        if(iterator.hasNext()) {
            Double previous = iterator.next();
            total = 1 + previous;
            while (iterator.hasNext()) {
                Double next = iterator.next();
                total *= 1 + (next - previous);
                previous = next;
            }
        }
        return total;
    }

    private List<Rating> normalizeRatings(List<Rating> stockStatistics) {
        if(stockStatistics.isEmpty()) {
            return stockStatistics;
        }
        LocalDate now = LocalDate.now();
        MaxMinAggregator apyGrown = new MaxMinAggregator();
        MaxMinAggregator discount = new MaxMinAggregator();
        MaxMinAggregator earningValue = new MaxMinAggregator();
        MaxMinAggregator hyperbolic = new MaxMinAggregator();
        stockStatistics.forEach(rating -> {
            apyGrown.add(rating.getApyGrown());
            discount.add(rating.getDiscount());
            earningValue.add(rating.getEarningValue());
            hyperbolic.add(rating.getHyperbolic());
        });

        MaxMinAggregator beauty = new MaxMinAggregator();
        List<RatingModel> ratingModels = stockStatistics.stream().map(rating -> {
            Double apyGrownNormalized = normalizeSingleValue(rating.getApyGrown(), apyGrown);
            Double discountNormalized = normalizeSingleValue(rating.getDiscount(), discount);
            Double earningValueNormalized = normalizeSingleValue(rating.getEarningValue(), earningValue);
            Double hyperbolicNormalized = normalizeSingleValue(rating.getHyperbolic(), hyperbolic);
            Double beautyValue = (1 + apyGrownNormalized) * (1 + discountNormalized) * (1 + earningValueNormalized) * (1 + hyperbolicNormalized);
            beauty.add(beautyValue);
            return RatingModel.builder()
                    .setStockCode(rating.getStockCode())
                    .setHyperbolic(hyperbolicNormalized)
                    .setEarningValue(earningValueNormalized)
                    .setDiscount(discountNormalized)
                    .setApyGrown(apyGrownNormalized)
                    .setBeauty(beautyValue)
                    .build();
        }).collect(Collectors.toList());

        return ratingModels.stream()
                .map(ratingModel -> {
                    Double beautyNormalized = normalizeSingleValue(ratingModel.getBeauty(), beauty);
                    return ratingModel.toBuilder()
                            .setBeauty(beautyNormalized)
                            .setDate(now)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Double normalizeSingleValue(Double value, MaxMinAggregator stats) {
        return (value - stats.getMin()) / (stats.getMax() - stats.getMin());
    }

    private Flux<Rating> persistRatings(Flux<Rating> rating) {
        return ratingService.saveAll(rating);
    }
}
