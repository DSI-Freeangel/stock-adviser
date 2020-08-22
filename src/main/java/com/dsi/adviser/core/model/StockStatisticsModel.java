package com.dsi.adviser.core.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockStatisticsModel implements StockStatistics {
    String stockCode;
    Double enterpriseValue;
    Double earnings;
    Double dividendsApy;
    Double priceLast;
    Double priceMinYtd;
    Double priceMaxYtd;
    Double priceYtd;
    List<Double> yearsPriceAvg;
}
