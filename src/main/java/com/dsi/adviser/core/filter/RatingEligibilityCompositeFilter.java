package com.dsi.adviser.core.filter;

import com.dsi.adviser.core.FinancialDataUpdateProcessor;
import com.dsi.adviser.financial.FinancialData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingEligibilityCompositeFilter {
    public final List<RatingEligibilityFilter> filters;
    private final FinancialDataUpdateProcessor financialDataUpdateProcessor;

    public Flux<String> filter(Flux<String> stockCodes) {
        Flux<FinancialData> filtered = stockCodes
                .flatMap(financialDataUpdateProcessor::updateFinancialDataIfNeeded, 1,1);
        for (RatingEligibilityFilter filter: filters) {
            filtered = filtered.transformDeferred(filter::filter);
        }
        return filtered
                .map(FinancialData::getStockCode);
    }
}
