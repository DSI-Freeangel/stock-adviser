package com.dsi.adviser.integration.client.alphavantage;

import com.dsi.adviser.integration.client.*;
import com.dsi.adviser.integration.client.alphavantage.model.ErrorResponse;
import com.dsi.adviser.integration.client.alphavantage.model.PriceItem;
import com.dsi.adviser.integration.client.alphavantage.model.PriceSeries;
import com.dsi.adviser.integration.financialData.Source;
import com.dsi.adviser.stock.RemoveStockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean({AlphaVantageProperties.class})
public class AlphaVantageClient implements PriceHistorySource, FinancialDataSource {
    private static final int SHORT_PERIOD = 100;
    private final AVWebClientFactory webClientFactory;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RemoveStockService removeStockService;
    private final ObjectMapper objectMapper;
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService continueExecutor = Executors.newFixedThreadPool(2);
    private WebClient webClient;

    @Override
    public Mono<FinancialDataItem> getFinancialData(String stockCode) {
        return Mono.just(stockCode)
                .publishOn(Schedulers.fromExecutor(requestExecutor))
                .transformDeferred(this::rateLimit)
                .doOnNext(stock -> log.info(String.format("Going to get financial data for stock '%s'", stock)))
                .flatMap(this::getFinancialDataResponse)
                .doOnNext(responseString -> handleErrorResponses(responseString, stockCode))
                .publishOn(Schedulers.fromExecutor(continueExecutor))
                .map(responseString -> toFinancialData(responseString, stockCode))
                .retry(3)
                .onErrorResume(this::handleLimitError);
    }

    private Mono<FinancialDataItem> handleLimitError(Throwable throwable) {
        if(throwable instanceof FrequencyLimitException) {
            log.info("Ignoring after 3 retries");
            return Mono.empty();
        }
        return Mono.error(throwable);
    }

    @SneakyThrows
    private Mono<String> handleErrorResponses(String responseString, String stockCode) {
        ErrorResponse priceSeries = objectMapper.readValue(responseString, ErrorResponse.class);
        if(priceSeries.getNote() != null) {
            log.info("API response note: {}", priceSeries.getNote());
            throw new FrequencyLimitException(priceSeries.getNote());
        }
        if(priceSeries.getInformation() != null) {
            log.info("API response information: {}", priceSeries.getInformation());
            throw new FrequencyLimitException(priceSeries.getInformation());
        }
        if(priceSeries.getErrorMessage() != null) {
            log.info("Error message returned: {} ", priceSeries.getErrorMessage());
            return removeStockService.removeByCode(stockCode).then(Mono.just(responseString));
        }
        return Mono.just(responseString);
    }

    private Mono<String> getFinancialDataResponse(String stockCode) {
        return getWebClient().get().uri(uriBuilder -> uriBuilder.path("/query")
                .queryParam("function", "OVERVIEW")
                .queryParam("symbol", stockCode)
                .queryParam("apikey", "{apikey}")
                .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public Flux<PriceDataItem> getPriceHistory(String stockCode, LocalDate fromDate) {
        return Mono.just(stockCode)
                .publishOn(Schedulers.fromExecutor(requestExecutor))
                .transformDeferred(this::rateLimit)
                .doOnNext(stock -> log.info(String.format("Going to get price history for stock '%s'", stock)))
                .flatMap(stock -> getPriceSeriesResponse(stock, fromDate))
                .doOnNext(responseString -> handleErrorResponses(responseString, stockCode))
                .publishOn(Schedulers.fromExecutor(continueExecutor))
                .map(this::getPriceSeries)
                .flatMapIterable(response -> getEntries(stockCode, response))
                .map(this::toPriceDataModel)
                .retry(3);
    }

    private Set<Map.Entry<LocalDate, PriceItem>> getEntries(String stockCode, PriceSeries response) {
        if(null != response.getPrices()) {
            return response.getPrices().entrySet();
        }
        log.info("No price data found for {} !", stockCode);
        return new HashSet<>();
    }

    private Mono<String> getPriceSeriesResponse(String stockCode, LocalDate fromDate) {
        return getWebClient().get().uri(uriBuilder -> buildPriceHistoryUrl(stockCode, fromDate, uriBuilder))
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }

    @SneakyThrows
    private PriceSeries getPriceSeries(String string) {
        return objectMapper.readValue(string, PriceSeries.class);
    }

    private URI buildPriceHistoryUrl(String stockCode, LocalDate fromDate, UriBuilder uriBuilder) {
        UriBuilder builder = uriBuilder.path("/query")
                .queryParam("function", "TIME_SERIES_DAILY")
                .queryParam("symbol", stockCode)
                .queryParam("apikey", "{apikey}");
        Optional.of(fromDate)
                .map(date -> ChronoUnit.DAYS.between(date, LocalDate.now()))
                .filter(days -> days > SHORT_PERIOD)
                .ifPresent(y -> builder.queryParam("outputsize", "full"));
        return builder.build();
    }

    private RateLimiter getRateLimiter() {
        return rateLimiterRegistry.rateLimiter(AlphaVantageConfiguration.API);
    }

    private Mono<String> rateLimit(Mono<String> input) {
        return input.doOnNext(v -> getRateLimiter().acquirePermission());
    }

    private PriceDataItem toPriceDataModel(Map.Entry<LocalDate, PriceItem> localDatePriceItemEntry) {
        PriceDataModel.PriceDataModelBuilder modelBuilder = PriceDataModel.builder()
                .setDate(localDatePriceItemEntry.getKey());
        BeanUtils.copyProperties(localDatePriceItemEntry.getValue(), modelBuilder);
        return modelBuilder.build();
    }

    private FinancialDataItem toFinancialData(String responseString, String stockCode) {
        return FinancialDataModel.builder()
                .setDate(LocalDate.now())
                .setJsonResponse(responseString)
                .setStockCode(stockCode)
                .setSource(Source.ALPHAVANTAGE)
                .build();
    }

    private WebClient getWebClient() {
        if(null == webClient) {
            webClient = webClientFactory.getWebClient();
        }
        return webClient;
    }
}
