package com.dsi.adviser.integration.client;

import reactor.core.publisher.Mono;

public interface FinancialDataSource {
    Mono<FinancialDataItem> getFinancialData(String stockCode);
}