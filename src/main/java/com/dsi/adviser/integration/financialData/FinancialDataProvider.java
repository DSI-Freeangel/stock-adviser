package com.dsi.adviser.integration.financialData;

import reactor.core.publisher.Mono;

public interface FinancialDataProvider {
    Mono<StockOverviewData> getFinancialData(String stockCodeFull);
}