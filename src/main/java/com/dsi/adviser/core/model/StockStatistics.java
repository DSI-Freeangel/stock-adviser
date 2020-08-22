package com.dsi.adviser.core.model;

import java.util.List;

public interface StockStatistics {
    String getStockCode();
    Double getEnterpriseValue();
    Double getEarnings();
    Double getDividendsApy();
    Double getPriceLast();
    Double getPriceMinYtd();
    Double getPriceMaxYtd();
    Double getPriceYtd();
    List<Double> getYearsPriceAvg();
}
