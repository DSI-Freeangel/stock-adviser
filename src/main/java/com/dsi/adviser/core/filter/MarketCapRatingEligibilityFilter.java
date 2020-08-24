package com.dsi.adviser.core.filter;

import com.dsi.adviser.financial.FinancialData;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Setter
@ConfigurationProperties("rating.eligibility.market-cap")
@ConditionalOnProperty(prefix = "rating.eligibility.market-cap", name = "enabled", havingValue = "true")
public class MarketCapRatingEligibilityFilter implements RatingEligibilityFilter {
    private Double threshold;
    @Override
    public Flux<FinancialData> filter(Flux<FinancialData> financialData) {
        return financialData.filter(data -> data.getMarketCapitalization() > threshold);
    }
}
