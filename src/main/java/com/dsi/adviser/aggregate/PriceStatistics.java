package com.dsi.adviser.aggregate;

import java.util.List;

public interface PriceStatistics {
    Double getPriceLast();
    Double getPriceMinYtd();
    Double getPriceMaxYtd();
    Double getPriceYtd();
    List<Double> getYearsPriceAvg();
}
