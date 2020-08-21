package com.dsi.adviser.core;

import com.dsi.adviser.financial.FinancialData;
import com.dsi.adviser.financial.FinancialService;
import com.dsi.adviser.integration.financialData.FinancialDataProvider;
import com.dsi.adviser.integration.financialData.StockOverviewData;
import com.dsi.adviser.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class FinancialDataUpdateProcessor {
    private final FinancialDataProvider financialDataProvider;
    private final FinancialService financialService;
    private final StockService stockService;

    public Mono<FinancialData> updateFinancialDataIfNeeded(String stockCodeFull) {
        return financialService.findOneByStockCode(stockCodeFull)
                .filter(financialData -> ChronoUnit.DAYS.between(financialData.getUpdatedDate().toLocalDate(), LocalDate.now()) < 30)
                .switchIfEmpty(this.updateFinancialData(stockCodeFull));
    }

    private Mono<FinancialData> updateFinancialData(String stockCodeFull) {
        return financialDataProvider.getFinancialData(stockCodeFull)
                .transformDeferred(this::updateStock)
                .flatMap(financialService::save);
    }

    private Mono<StockOverviewData> updateStock(Mono<StockOverviewData> stockOverview) {
        Mono<StockOverviewData> cache = stockOverview.cache();
        return cache.flatMap(stockService::updateStockIndustryData)
                .then(cache);
    }
}
