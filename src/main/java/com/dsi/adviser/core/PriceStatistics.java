package com.dsi.adviser.core;

import java.util.List;

public interface PriceStatistics {
    Double getPriceLast();
    Double getPriceMinYtd();
    Double getPriceMaxYtd();
    Double getPriceYtd();
    List<Double> getYearsPriceAvg();
}
