package com.dsi.adviser.integration.client.alphavantage;

import com.dsi.adviser.integration.client.FinancialDataItem;
import com.dsi.adviser.integration.client.PriceDataItem;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

public class AlphaVantageClientTest {
    private static final String STOCK_CODE_FULL = "NYSE:IBM";
    private final AlphaVantageProperties demo = new AlphaVantageProperties("https://www.alphavantage.co/", "86T2JAZAYN5Q24FS");
    private final AVWebClientFactory webClientFactory = new AVWebClientFactory(demo);
    private final AlphaVantageClient client = new AlphaVantageClient(webClientFactory);

    @Test
    public void testFinancialDataReceived() {
        Mono<FinancialDataItem> financialData = client.getFinancialData(STOCK_CODE_FULL);

        StepVerifier.create(financialData)
                .expectNextMatches(financialDataItem -> financialDataItem.getDate().equals(LocalDate.now())
                        && financialDataItem.getJsonResponse().contains("\"Symbol\": \"IBM\",")
                        && financialDataItem.getStockCodeFull().equals(STOCK_CODE_FULL))
                .verifyComplete();
    }

    @Test
    public void testShortTimePriceSeriesReceived() {
        Flux<PriceDataItem> priceHistory = client.getPriceHistory(STOCK_CODE_FULL, LocalDate.now().minusDays(99));
        StepVerifier.create(priceHistory)
                .expectNextMatches(data -> isValidData(data))
                .expectNextCount(99)
        .verifyComplete();
    }

    @Test
    public void testFullPriceSeriesReceived() {
        Flux<PriceDataItem> priceHistory = client.getPriceHistory(STOCK_CODE_FULL, LocalDate.now().minusDays(101));
        StepVerifier.create(priceHistory)
                .expectNextCount(101)
                .thenConsumeWhile(data -> isValidData(data))
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
