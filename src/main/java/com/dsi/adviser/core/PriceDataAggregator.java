package com.dsi.adviser.core;

import com.dsi.adviser.price.PriceData;
import com.dsi.adviser.price.PriceModel;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;

@RequiredArgsConstructor
public class PriceDataAggregator implements BiFunction<PriceData, PriceData, PriceData> {
    private double sum = 0.0;
    private int count = 0;

    @Override
    public PriceData apply(PriceData first, PriceData second) {
        if(count == 0) {
            sum += first.getPrice();
            count ++;
        }
        sum += second.getPrice();
        count ++;
        return PriceModel.builder()
                .setStockCodeFull(first.getStockCodeFull())
                .setType(first.getType())
                .setDate(first.getDate())
                .setPriceMin(Math.min(first.getPriceMin(), second.getPriceMin()))
                .setPriceMax(Math.max(first.getPriceMax(), second.getPriceMax()))
                .setPrice(Math.round(100 * sum/count) / 100.0)
                .build();
    }

}
