package com.dsi.adviser.core;

import com.dsi.adviser.rating.Rating;
import com.dsi.adviser.rating.RatingModel;
import com.dsi.adviser.rating.RatingService;
import com.dsi.adviser.stock.Stock;
import com.dsi.adviser.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingCalculationProcessor {
    private final StockStatisticsSource stockStatisticsSource;
    private final StockService stockService;
    private final RatingService ratingService;

    public Flux<Rating> updateRating() {
        return stockService.findAll()
                .map(Stock::getStockCodeFull)
                .flatMap(stockStatisticsSource::getStatistics)
                .map(this::prepareCoefficients)
                .collectList()
                .flatMapIterable(this::normalizeRatings)
                .window(1000)
                .flatMap(this::persistRatings);
    }

    private Rating prepareCoefficients(StockStatistics stockStatistics) {
        Double yearlyGrown = (stockStatistics.getPriceLast() - stockStatistics.getPriceYtd()) / stockStatistics.getPriceYtd();
        Double apyGrown = stockStatistics.getDividendsApy() + yearlyGrown;
        Double discount = (stockStatistics.getPriceMaxYtd() - stockStatistics.getPriceMinYtd()) / (stockStatistics.getPriceMaxYtd() - stockStatistics.getPriceMinYtd());
        Double earningValue = stockStatistics.getEarnings() / stockStatistics.getEnterpriseValue();
        Double hyperbolic = calculateHyperbolic(stockStatistics.getYearsPriceAvg());
        return RatingModel.builder()
                .setStockCodeFull(stockStatistics.getStockCodeFull())
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
        Double previous = iterator.next();
        Double total = 1 + previous;
        while (iterator.hasNext()) {
            Double next = iterator.next();
            total *= 1 + (next - previous);
        }
        return total;
    }

    private List<Rating> normalizeRatings(List<Rating> stockStatistics) {
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
            Double beautyValue = apyGrownNormalized * discountNormalized * earningValueNormalized * hyperbolicNormalized;
            beauty.add(beautyValue);
            return RatingModel.builder()
                    .setStockCodeFull(rating.getStockCodeFull())
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
        return ratingService.deleteAllByDate(LocalDate.now())
                .thenMany(ratingService.saveAll(rating));
    }
}
