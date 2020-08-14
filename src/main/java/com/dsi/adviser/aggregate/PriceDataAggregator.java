package com.dsi.adviser.aggregate;

import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceModel;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class PriceDataAggregator implements BiFunction<PriceData, PriceData, PriceData> {
    private final LocalDate date;
    private final Period period;
    private double sum = 0.0;
    private int count = 0;

    @Override
    public PriceData apply(PriceData first, PriceData second) {
        if(count == 0) {
            sum += first.getPrice();
        }
        sum += second.getPrice();
        count ++;
        return PriceModel.builder()
                .setStockCodeFull(first.getStockCodeFull())
                .setType(period)
                .setDate(date)
                .setPriceMin(Math.min(first.getPriceMin(), second.getPriceMin()))
                .setPriceMax(Math.max(first.getPriceMax(), second.getPriceMax()))
                .setPrice(Math.round(100 * sum/count) / 100.0)
                .build();
    }

}
