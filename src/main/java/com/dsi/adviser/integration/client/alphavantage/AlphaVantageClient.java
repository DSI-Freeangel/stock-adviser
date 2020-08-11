package com.dsi.adviser.integration.client.alphavantage;

import com.dsi.adviser.integration.client.*;
import com.dsi.adviser.integration.client.alphavantage.model.PriceItem;
import com.dsi.adviser.integration.client.alphavantage.model.PriceSeries;
import com.dsi.adviser.integration.financialData.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
@ConditionalOnBean({AlphaVantageProperties.class})
public class AlphaVantageClient implements PriceHistorySource, FinancialDataSource {
    public static final int SHORT_PERIOD = 100;
    private final AVWebClientFactory webClientFactory;
    private WebClient webClient;

    @Override
    public Mono<FinancialDataItem> getFinancialData(String stockCodeFull) {
        return getWebClient().get().uri(uriBuilder -> uriBuilder.path("/query")
                    .queryParam("function", "OVERVIEW")
                    .queryParam("symbol", getStockCode(stockCodeFull))
                    .queryParam("apikey", "{apikey}")
                    .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .map(responseString -> toFinancialData(responseString, stockCodeFull));
    }

    @Override
    public Flux<PriceDataItem> getPriceHistory(String stockCodeFull, LocalDate fromDate) {
        return getWebClient().get().uri(uriBuilder -> buildPriceHistoryUrl(stockCodeFull, fromDate, uriBuilder))
                .accept(APPLICATION_JSON)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(PriceSeries.class))
                .flatMapIterable(response -> response.getPrices().entrySet())
                .map(this::toPriceDataModel);
    }

    private URI buildPriceHistoryUrl(String stockCodeFull, LocalDate fromDate, UriBuilder uriBuilder) {
        UriBuilder builder = uriBuilder.path("/query")
                .queryParam("function", "TIME_SERIES_DAILY")
                .queryParam("symbol", getStockCode(stockCodeFull))
                .queryParam("apikey", "{apikey}");
        Optional.of(fromDate)
                .map(date -> ChronoUnit.DAYS.between(date, LocalDate.now()))
                .filter(days -> days > SHORT_PERIOD)
                .ifPresent(y -> builder.queryParam("outputsize", "full"));
        return builder.build();
    }

    private PriceDataItem toPriceDataModel(Map.Entry<LocalDate, PriceItem> localDatePriceItemEntry) {
        PriceDataModel.PriceDataModelBuilder modelBuilder = PriceDataModel.builder()
                .setDate(localDatePriceItemEntry.getKey());
        BeanUtils.copyProperties(localDatePriceItemEntry.getValue(), modelBuilder);
        return modelBuilder.build();
    }

    private String getStockCode(String stockCodeFull) {
        return stockCodeFull.split(":")[1];
    }

    private FinancialDataItem toFinancialData(String responseString, String stockCodeFull) {
        return FinancialDataModel.builder()
                .setDate(LocalDate.now())
                .setJsonResponse(responseString)
                .setStockCodeFull(stockCodeFull)
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
