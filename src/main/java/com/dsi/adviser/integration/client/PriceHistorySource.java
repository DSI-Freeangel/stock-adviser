package com.dsi.adviser.integration.client;

import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface PriceHistorySource {
    Flux<PriceDataItem> getPriceHistory(String stockCode, LocalDate fromDate);
}