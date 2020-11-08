package com.dsi.adviser.integration.client.alphavantage;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AlphaVantageConfiguration {

    @Bean
    @ConditionalOnBean({AlphaVantageProperties.class})
    public RateLimiterRegistry rateLimiterRegistry(AlphaVantageProperties properties) {
        InMemoryRateLimiterRegistry rateLimiterRegistry = new InMemoryRateLimiterRegistry(RateLimiterConfig.ofDefaults());
        for(String apiKey : properties.getApiKeys()) {
            rateLimiterRegistry.rateLimiter(apiKey, RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofSeconds(12))
                    .limitForPeriod(1)
                    .timeoutDuration(Duration.ofHours(1))
                    .build());
        }
        return rateLimiterRegistry;
    }
}
