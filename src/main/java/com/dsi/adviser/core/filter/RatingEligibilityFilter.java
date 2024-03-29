package com.dsi.adviser.core.filter;

import com.dsi.adviser.financial.FinancialData;
import reactor.core.publisher.Flux;

public interface RatingEligibilityFilter {
    Flux<FinancialData> filter(Flux<FinancialData> financialData);
}
