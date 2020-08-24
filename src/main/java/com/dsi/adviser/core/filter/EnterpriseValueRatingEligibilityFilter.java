package com.dsi.adviser.core.filter;

import com.dsi.adviser.financial.FinancialData;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Setter
@ConfigurationProperties("rating.eligibility.enterprise-value")
@ConditionalOnProperty(prefix = "rating.eligibility.enterprise-value", name = "enabled", havingValue = "true")
public class EnterpriseValueRatingEligibilityFilter implements RatingEligibilityFilter {
    private Double threshold;
    @Override
    public Flux<FinancialData> filter(Flux<FinancialData> financialData) {
        return financialData.filter(data -> data.getEnterpriseValue() > threshold);
    }
}
