package com.dsi.adviser.integration.client.alphavantage;

import com.dsi.adviser.integration.client.FinancialDataItem;
import com.dsi.adviser.integration.client.PriceDataItem;
import com.dsi.adviser.stock.RemoveStockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlphaVantageClientTest {
    private static final String STOCK_CODE = "IBM";
    private static final String KEY = "86T2JAZAYN5Q24FS";
    private final AlphaVantageProperties demo = new AlphaVantageProperties("https://www.alphavantage.co/", Lists.newArrayList(KEY));
    private final AVWebClientFactory webClientFactory = new AVWebClientFactory(demo);
    private final InMemoryRateLimiterRegistry rateLimiterRegistry = new InMemoryRateLimiterRegistry(RateLimiterConfig.ofDefaults());
    private final WebClientProvider webClientProvider = new WebClientProvider(demo,rateLimiterRegistry, webClientFactory);
    private final RemoveStockService removeStockService = mock(RemoveStockService.class);
    private final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final AlphaVantageClient client = new AlphaVantageClient(webClientProvider, removeStockService,  MAPPER);

    @Before
    public void setUp() {
        rateLimiterRegistry.rateLimiter(KEY, RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofHours(1))
                .build());
        when(removeStockService.removeByCode(any())).thenReturn(Mono.empty());
    }

    @Test
    public void testFinancialDataReceived() {
        Mono<FinancialDataItem> financialData = client.getFinancialData(STOCK_CODE);

        StepVerifier.create(financialData)
                .expectNextMatches(financialDataItem -> financialDataItem.getDate().equals(LocalDate.now())
                        && financialDataItem.getJsonResponse().contains("\"Symbol\": \"IBM\",")
                        && financialDataItem.getStockCode().equals(STOCK_CODE))
                .verifyComplete();
    }

    @Test
    public void testShortTimePriceSeriesReceived() {
        Flux<PriceDataItem> priceHistory = client.getPriceHistory(STOCK_CODE, LocalDate.now().minusDays(99));
        StepVerifier.create(priceHistory)
                .expectNextMatches(this::isValidData)
                .expectNextCount(99)
        .verifyComplete();
    }

    @Test
    public void testFullPriceSeriesReceived() {
        Flux<PriceDataItem> priceHistory = client.getPriceHistory(STOCK_CODE, LocalDate.now().minusDays(101));
        StepVerifier.create(priceHistory)
                .expectNextCount(101)
                .thenConsumeWhile(this::isValidData)
                .verifyComplete();
    }

    private boolean isValidData(PriceDataItem data) {
        return data.getDate() != null
                && data.getPriceClose() > 0
                && data.getPriceOpen() > 0
                && data.getVolume() > 0
                && data.getPriceMin() > 0
                && data.getPriceMax() > 0
                && data.getPriceMax() >= data.getPriceMin();
    }
}
