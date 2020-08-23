package com.dsi.adviser.core;

import com.dsi.adviser.core.model.StockStatistics;
import com.dsi.adviser.core.model.StockStatisticsModel;
import com.dsi.adviser.financial.FinancialData;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockStatisticsExtractor {
    private final PriceService priceService;
    private final FinancialDataUpdateProcessor financialDataUpdateProcessor;

    public Mono<StockStatistics> extract(String stockCode) {
        LocalDate now = LocalDate.now();
        LocalDate fiveYearsAgo = now.minusYears(5);
        LocalDate oneYearAgo = now.minusYears(1);
        Mono<List<Double>> prices = priceService.findPricesForInterval(stockCode, Period.YEAR, fiveYearsAgo, now)
                .map(PriceData::getPrice)
                .switchIfEmpty(Flux.error(new NoPriceDataException("No price data for stock '" + stockCode + "'")))
                .collectList();
        Mono<List<Double>> yearPrices = priceService.findPricesForInterval(stockCode, Period.DAY, oneYearAgo, now)
                .map(PriceData::getPrice)
                .switchIfEmpty(Flux.error(new NoPriceDataException("No price data for stock '" + stockCode + "'")))
                .collectList();
        Mono<FinancialData> financialData = financialDataUpdateProcessor.updateFinancialDataIfNeeded(stockCode);
        return Mono.zip(financialData, prices, yearPrices)
                .map(tuple -> buildPriceStatistics(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private StockStatistics buildPriceStatistics(FinancialData financialData, List<Double> priceDataList, List<Double> yearPrices) {
        StockStatisticsModel.StockStatisticsModelBuilder builder = StockStatisticsModel.builder()
                .setStockCode(financialData.getStockCode())
                .setEnterpriseValue(financialData.getEnterpriseValue())
                .setEarnings(financialData.getEarnings())
                .setDividendsApy(financialData.getDividendsApy())
                .setYearsPriceAvg(priceDataList)
                .setPriceYtd(yearPrices.get(0))
                .setPriceLast(yearPrices.get(yearPrices.size() - 1));
        Double priceMin = financialData.getPriceMinYtd();
        Double priceMax = financialData.getPriceMaxYtd();
        for(Double price: yearPrices) {
            if(price < priceMin) {
                priceMin = price;
            } else if(price > priceMax) {
                priceMax = price;
            }
        }
        return builder.setPriceMinYtd(priceMin)
                .setPriceMaxYtd(priceMax)
                .build();
    }
}