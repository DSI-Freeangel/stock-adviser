package com.dsi.adviser.integration.client.alphavantage;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AlphaVantageConfiguration {
    public static final String API = "API";
    //TODO: Make it configurable

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        InMemoryRateLimiterRegistry rateLimiterRegistry = new InMemoryRateLimiterRegistry(RateLimiterConfig.ofDefaults());
        rateLimiterRegistry.rateLimiter(API, RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(12))
                .limitForPeriod(1)
                .timeoutDuration(Duration.ofHours(1))
                .build());
        return rateLimiterRegistry;
    }
}
