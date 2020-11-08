package com.dsi.adviser.integration.client.alphavantage;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean({AlphaVantageProperties.class})
public class WebClientProvider {
    private final AtomicInteger index = new AtomicInteger(0);
    private final AlphaVantageProperties properties;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final AVWebClientFactory webClientFactory;
    private final Map<String, WebClient> clients = new HashMap<>();

    public WebClient next() {
        int size = properties.getApiKeys().size();
        int index = this.index.incrementAndGet() % size;
        this.index.set(index);
        String apiKey = properties.getApiKeys().get(index);
        WebClient webClient = clients.computeIfAbsent(apiKey, webClientFactory::getWebClient);
        getRateLimiter(apiKey).acquirePermission();
        return webClient;
    }

    private RateLimiter getRateLimiter(String key) {
        return rateLimiterRegistry.rateLimiter(key);
    }
}
