package com.dsi.adviser.integration.financialData;

import com.dsi.adviser.financial.FinancialData;
import reactor.core.publisher.Mono;

public interface FinancialDataProvider {
    Mono<FinancialData> getFinancialData(String stockCodeFull);

    Mono<Void> updateFinancialData(String stockCodeFull);
}