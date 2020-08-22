package com.dsi.adviser.integration.financialData;

import com.dsi.adviser.integration.client.FinancialDataItem;
import com.dsi.adviser.integration.client.FinancialDataModel;
import com.dsi.adviser.integration.client.FinancialDataSource;
import com.dsi.adviser.integration.financialData.parser.FinancialDataParser;
import com.dsi.adviser.integration.financialData.parser.FinancialParserProvider;
import com.dsi.adviser.integration.financialData.parser.RawFinancialData;
import com.dsi.adviser.integration.financialData.parser.alphavantage.AlphavantageFinancialData;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FinancialDataProviderImplTest {
    private static final String STOCK_CODE = "IBM";
    private static final LocalDate DATE = LocalDate.of(2020, 6, 30);
    private static final String NAME = "International Business Machines Corporation";
    private static final String INDUSTRY = "Information Technology Services";
    private static final String SECTOR = "Technology";
    private static final double ENTERPRISE_VALUE = 15576999936.0 * 11.18;
    private static final double EARNINGS = 15576999936.0;
    private static final double DIVIDENDS_APY = 0.0522;
    private static final double PRICE_MIN_YTD = 90.56;
    private static final double PRICE_MAX_YTD = 158.75;
    private static final RawFinancialData RAW_FINANCIAL_DATA = AlphavantageFinancialData.builder()
            .setName(NAME)
            .setIndustry(INDUSTRY)
            .setSector(SECTOR)
            .setDate(DATE)
            .setEnterpriseValue(ENTERPRISE_VALUE)
            .setEarnings(EARNINGS)
            .setDividendsApy(DIVIDENDS_APY)
            .setPriceMinYtd(PRICE_MIN_YTD)
            .setPriceMaxYtd(PRICE_MAX_YTD)
            .build();
    private final FinancialDataSource dataSource = mock(FinancialDataSource.class);
    private final FinancialDataRepository financialDataRepository = mock(FinancialDataRepository.class);
    private final FinancialParserProvider financialParserProvider = mock(FinancialParserProvider.class);
    private final FinancialDataParser financialDataParser = mock(FinancialDataParser.class);
    private final FinancialDataProviderImpl financialDataProvider = new FinancialDataProviderImpl(dataSource, financialDataRepository, financialParserProvider);

    @Before
    public void init() {
        when(financialParserProvider.getParser(any())).thenReturn(financialDataParser);
        when(financialDataParser.parse(any())).thenReturn(RAW_FINANCIAL_DATA);
        when(financialDataRepository.save(any())).then(i -> Mono.just(i.getArgument(0, FinancialDataEntity.class)));
    }

    @Test
    public void getFinancialDataWhenDataActualInDB() {
        givenFinancialDataPresentInDB(STOCK_CODE, LocalDate.now().minusDays(3));
        givenDataSourceIsAbleToReturnEmptyFinancialData();
        Mono<StockOverviewData> financialData = financialDataProvider.getFinancialData(STOCK_CODE);

        StepVerifier.create(financialData)
                .expectNextMatches(this::validateStockData)
                .verifyComplete();
    }

    @Test
    public void getFinancialDataWhenNoDataInDB() {
        givenFinancialDataIsNotPresentInDB(STOCK_CODE);
        givenDataSourceIsAbleToReturnFinancialData();
        Mono<StockOverviewData> financialData = financialDataProvider.getFinancialData(STOCK_CODE);

        StepVerifier.create(financialData)
                .expectNextMatches(this::validateStockData)
                .verifyComplete();
    }

    @Test
    public void getFinancialDataWhenDataIsNotActualInDB() {
        givenFinancialDataPresentInDB(STOCK_CODE, LocalDate.now().minusDays(31));
        givenDataSourceIsAbleToReturnFinancialData();
        Mono<StockOverviewData> financialData = financialDataProvider.getFinancialData(STOCK_CODE);

        StepVerifier.create(financialData)
                .expectNextMatches(this::validateStockData)
                .verifyComplete();
    }

    private boolean validateStockData(StockOverviewData stockOverviewData) {
        return stockOverviewData.getStockCode().equals(STOCK_CODE)
                && stockOverviewData.getDate().equals(DATE)
                && stockOverviewData.getName().equals(NAME)
                && stockOverviewData.getIndustry().equals(INDUSTRY)
                && stockOverviewData.getSector().equals(SECTOR)
                && stockOverviewData.getEnterpriseValue().equals(ENTERPRISE_VALUE)
                && stockOverviewData.getEarnings().equals(EARNINGS)
                && stockOverviewData.getDividendsApy().equals(DIVIDENDS_APY)
                && stockOverviewData.getPriceMinYtd().equals(PRICE_MIN_YTD)
                && stockOverviewData.getPriceMaxYtd().equals(PRICE_MAX_YTD);
    }

    private void givenFinancialDataPresentInDB(String stockCode, LocalDate date) {
        when(financialDataRepository.findOneByStockCodeEquals(eq(stockCode))).thenReturn(Mono.just(getFinancialDataEntity(stockCode, date)));
    }

    private void givenFinancialDataIsNotPresentInDB(String stockCode) {
        when(financialDataRepository.findOneByStockCodeEquals(eq(stockCode))).thenReturn(Mono.empty());
    }

    private void givenDataSourceIsAbleToReturnEmptyFinancialData() {
        when(dataSource.getFinancialData(eq(STOCK_CODE))).thenReturn(Mono.empty());
    }

    private void givenDataSourceIsAbleToReturnFinancialData() {
        when(dataSource.getFinancialData(eq(STOCK_CODE))).thenReturn(Mono.just(getFinancialDataItem()));
    }

    private FinancialDataItem getFinancialDataItem() {
        return FinancialDataModel.builder()
                .setSource(Source.ALPHAVANTAGE)
                .setStockCode(STOCK_CODE)
                .build();
    }

    private FinancialDataEntity getFinancialDataEntity(String stockCode, LocalDate date) {
        return FinancialDataEntity.builder()
                .setStockCode(stockCode)
                .setSource(Source.ALPHAVANTAGE)
                .setDate(date)
                .build();
    }
}