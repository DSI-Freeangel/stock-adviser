package com.dsi.adviser.financial;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.dsi.adviser.financial.TestData.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FinancialServiceTest {

    private final FinancialRepository financialRepository = mock(FinancialRepository.class);
    private final FinancialService financialService = new FinancialService(financialRepository);

    @Test
    public void testDataReceived() {
        givenEntityExistsInRepository();
        Mono<FinancialModel> model = financialService.findOneByStockCode(STOCK_CODE_FULL);
        verifyModel(model);
    }

    @Test
    public void testNoDataReceivedIfNotPresent() {
        givenEntityIsNotPresentInRepository();
        Mono<FinancialModel> model = financialService.findOneByStockCode(STOCK_CODE_FULL);
        verifyEmpty(model);
    }

    @Test
    public void testDataSaved() {
        givenEntityIsNotPresentInRepository();
        givenEntitySavingWorks();
        Mono<FinancialModel> model = financialService.save(FINANCIAL_MODEL);
        verifyModel(model);
    }

    @Test
    public void testDataUpdated() {
        givenEntityExistsInRepository();
        givenEntitySavingWorks();
        Mono<FinancialModel> model = financialService.save(FINANCIAL_MODEL);
        verifyModel(model);
    }

    private void givenEntitySavingWorks() {
        when(financialRepository.save(eq(FINANCIAL_ENTITY))).thenReturn(Mono.just(FINANCIAL_ENTITY.toBuilder().setId(1L).build()));
    }

    private void givenEntityExistsInRepository() {
        when(financialRepository.findOneByStockCodeFullEquals(eq(STOCK_CODE_FULL))).thenReturn(Mono.just(FINANCIAL_ENTITY));
    }

    private void givenEntityIsNotPresentInRepository() {
        when(financialRepository.findOneByStockCodeFullEquals(eq(STOCK_CODE_FULL))).thenReturn(Mono.empty());
    }

    private void verifyModel(Mono<FinancialModel> model) {
        StepVerifier.create(model)
                .expectNextMatches(result ->
                        result.getStockCodeFull().equals(STOCK_CODE_FULL)
                        && result.getYear().equals(YEAR)
                        && result.getEnterpriseValue().equals(ENTERPRISE_VALUE)
                        && result.getEarnings().equals(EARNINGS)
                        && result.getDividendsApy().equals(DIVIDENDS_APY)
                        && result.getPriceMinYtd().equals(PRICE_MIN_YTD)
                        && result.getPriceMaxYtd().equals(PRICE_MAX_YTD))
                .verifyComplete();
    }

    private void verifyEmpty(Mono<FinancialModel> model) {
        StepVerifier.create(model)
                .expectNextCount(0)
                .verifyComplete();
    }
}
