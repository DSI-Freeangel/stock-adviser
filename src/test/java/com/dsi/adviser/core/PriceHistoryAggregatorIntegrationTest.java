package com.dsi.adviser.core;

import com.dsi.adviser.integration.client.alphavantage.AVWebClientFactory;
import com.dsi.adviser.integration.priceData.PriceDataRepository;
import com.dsi.adviser.price.Period;
import com.dsi.adviser.price.PriceEntity;
import com.dsi.adviser.price.PriceRepository;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class PriceHistoryAggregatorIntegrationTest {
    private static final int BUFFER_SIZE = 20 * 1024 * 1024;
    private static final String STOCK_CODE_FULL = "NYSE:IBM";
    private static final LocalDate FROM_DATE = LocalDate.of(1990, 1, 1);

    @Autowired
    private PriceHistoryAggregator priceHistoryAggregator;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private PriceDataRepository priceDataRepository;

    @MockBean
    private AVWebClientFactory webClientFactory;

    @Before
    @SneakyThrows
    public void init() {
        String responseString = new String(Files.readAllBytes(Paths.get(getClass().getResource("stock-prices-response.json").toURI())));
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(codec -> codec.defaultCodecs().maxInMemorySize(BUFFER_SIZE))
                .build();
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setDefaultUriVariables(new HashMap<String, String>(){{put("apikey", "demo");}});
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK, exchangeStrategies)
                        .header("content-type", "application/json")
                        .body(responseString)
                        .statusCode(HttpStatus.OK)
                        .build()))
                .uriBuilderFactory(uriBuilderFactory)
                .build();
        when(webClientFactory.getWebClient()).thenReturn(webClient);
    }

    @After
    public void tearDown() {
        priceRepository.deleteAll().block();
        System.out.println("PriceRepository clean!");
        priceDataRepository.deleteAll().block();
        System.out.println("PriceDataRepository clean!");
    }

    @Test
    public void testDataAggregated() {
        StopWatch timer = new StopWatch("Aggregation time");
        timer.start();
        priceHistoryAggregator.aggregate(STOCK_CODE_FULL).block();
        timer.stop();
        System.out.println("Aggregation time = " + timer.getTotalTimeSeconds() + "s");

        Long recordsCount = priceRepository.count().block();
        System.out.println("recordsCount = " + recordsCount);
        assertEquals(5587L, recordsCount.longValue());

        Flux<PriceEntity> yearsData = priceRepository.findAllByStockCodeFullAndTypeAndDateGreaterThanEqual(STOCK_CODE_FULL, Period.YEAR, FROM_DATE);

        StepVerifier.create(yearsData)
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(1999, 1, 1), 104.01, 90.0, 122.12))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2000, 1, 1), 110.58, 80.06, 134.94))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2001, 1, 1), 107.37, 83.75, 124.7))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2002, 1, 1), 84.52, 54.01, 126.39))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2003, 1, 1), 85.03, 73.17, 94.54))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2004, 1, 1), 90.82, 81.9, 100.43))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2005, 1, 1), 83.91, 71.85, 99.1))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2006, 1, 1), 83.15, 72.73, 97.88))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2007, 1, 1), 105.69, 88.77, 121.46))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2008, 1, 1), 110.0, 69.5, 130.93))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2009, 1, 1), 109.03, 81.76, 132.85))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2010, 1, 1), 131.78, 116.0, 147.53))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2011, 1, 1), 170.92, 146.64, 194.9))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2012, 1, 1), 196.56, 177.35, 211.79))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2013, 1, 1), 194.27, 172.57, 215.9))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2014, 1, 1), 182.24, 150.5, 199.21))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2015, 1, 1), 155.39, 131.65, 176.3))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2016, 1, 1), 150.22, 116.9, 169.95))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2017, 1, 1), 157.89, 139.13, 182.79))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2018, 1, 1), 143.81, 105.94, 171.13))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2019, 1, 1), 136.97, 111.69, 152.95))
                .expectNextMatches(data -> isValidRecord(data, STOCK_CODE_FULL, Period.YEAR, LocalDate.of(2020, 1, 1), 126.23, 90.56, 158.75))
                .verifyComplete();
    }

    private boolean isValidRecord(PriceEntity priceEntity,
                                  String stockCode,
                                  Period period,
                                  LocalDate date,
                                  double price,
                                  double priceMin,
                                  double priceMax) {
        return priceEntity.getStockCodeFull().equals(stockCode)
                && priceEntity.getType().equals(period)
                && priceEntity.getDate().equals(date)
                && priceEntity.getPrice().equals(price)
                && priceEntity.getPriceMin().equals(priceMin)
                && priceEntity.getPriceMax().equals(priceMax);
    }
}