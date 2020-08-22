package com.dsi.adviser.financial;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.dsi.adviser.financial.TestData.*;

@DataR2dbcTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class FinancialRepositoryTest {

    @Autowired
    private FinancialRepository repository;

    @After
    public void init() {
        repository.deleteAll().block();
        System.out.println("FinancialRepository clean!");
    }

    @Test
    public void testDataSavedToRepository() {
        Mono<FinancialEntity> saved = repository.save(FINANCIAL_ENTITY);
        verifyFinancialEntity(saved);
    }

    @Test
    public void testDataReceivedByCode() {
        repository.save(FINANCIAL_ENTITY).block();
        Mono<FinancialEntity> financialEntity = repository.findOneByStockCodeEquals(STOCK_CODE);
        verifyFinancialEntity(financialEntity);
    }

    private void verifyFinancialEntity(Mono<FinancialEntity> saved) {
        StepVerifier.create(saved)
                .expectNextMatches(result -> result.getId() != null
                        && result.getStockCode().equals(STOCK_CODE)
                        && result.getDate().equals(DATE)
                        && result.getEnterpriseValue().equals(ENTERPRISE_VALUE)
                        && result.getEarnings().equals(EARNINGS)
                        && result.getDividendsApy().equals(DIVIDENDS_APY)
                        && result.getPriceMinYtd().equals(PRICE_MIN_YTD)
                        && result.getPriceMaxYtd().equals(PRICE_MAX_YTD))
                .verifyComplete();
    }
}
