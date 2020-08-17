package com.dsi.adviser.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PriceStatisticsModel implements PriceStatistics {
    Double priceLast;
    Double priceMinYtd;
    Double priceMaxYtd;
    Double priceYtd;
    List<Double> yearsPriceAvg;
}
