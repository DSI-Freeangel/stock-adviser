package com.dsi.adviser.integration.priceData;

import com.dsi.adviser.price.PriceData;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface PriceDataProvider {
    Flux<PriceData> getPriceData(String stockCode, LocalDate fromDate);
}