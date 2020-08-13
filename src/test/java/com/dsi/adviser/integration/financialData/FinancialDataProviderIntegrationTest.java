package com.dsi.adviser.integration.financialData;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class FinancialDataProviderIntegrationTest {
    private static final String STOCK_CODE_FULL = "NYSE:IBM";
    private static final String NAME = "International Business Machines Corporation";
    private static final String INDUSTRY = "Information Technology Services";
    private static final String SECTOR = "Technology";

    @Autowired
    private FinancialDataRepository financialDataRepository;

    @Autowired
    private FinancialDataProviderImpl financialDataProvider;

    @After
    public void init() {
        financialDataRepository.deleteAll().block();
        System.out.println("FinancialDataRepository clean!");
    }

    @Test
    public void testActualDataSavedAndReturned() {
        Mono<StockOverviewData> financialData = financialDataProvider.getFinancialData(STOCK_CODE_FULL);

        StepVerifier.create(financialData)
                .expectNextMatches(this::isFinancialDataValid)
                .verifyComplete();

        Long count = financialDataRepository.count().block();
        assertEquals(1L, count.longValue());
    }

    private boolean isFinancialDataValid(StockOverviewData stockOverviewData) {
        return stockOverviewData.getStockCodeFull().equals(STOCK_CODE_FULL)
                && stockOverviewData.getDate() != null
                && stockOverviewData.getName().equals(NAME)
                && stockOverviewData.getIndustry().equals(INDUSTRY)
                && stockOverviewData.getSector().equals(SECTOR)
                && stockOverviewData.getEnterpriseValue() > 0
                && stockOverviewData.getEarnings() > 0
                && stockOverviewData.getDividendsApy() > 0
                && stockOverviewData.getPriceMinYtd() > 0
                && stockOverviewData.getPriceMaxYtd() > 0
                && stockOverviewData.getPriceMinYtd() < stockOverviewData.getPriceMaxYtd();
    }
}