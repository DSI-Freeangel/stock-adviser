package com.dsi.adviser.financial;

import java.time.LocalDate;

class TestData {
    static final String STOCK_CODE_FULL = "IBM";
    static final LocalDate DATE = LocalDate.of(2020, 1, 1);
    static final double ENTERPRISE_VALUE = 20.0;
    static final double EARNINGS = 1.0;
    static final double DIVIDENDS_APY = 1.0;
    static final double PRICE_MIN_YTD = 2.0;
    static final double PRICE_MAX_YTD = 3.0;

    static final FinancialEntity FINANCIAL_ENTITY = FinancialEntity.builder()
            .setStockCodeFull(STOCK_CODE_FULL)
            .setDate(DATE)
            .setEnterpriseValue(ENTERPRISE_VALUE)
            .setEarnings(EARNINGS)
            .setDividendsApy(DIVIDENDS_APY)
            .setPriceMinYtd(PRICE_MIN_YTD)
            .setPriceMaxYtd(PRICE_MAX_YTD)
            .build();

    static final FinancialModel FINANCIAL_MODEL = FinancialModel.builder()
            .setStockCodeFull(STOCK_CODE_FULL)
            .setDate(DATE)
            .setEnterpriseValue(ENTERPRISE_VALUE)
            .setEarnings(EARNINGS)
            .setDividendsApy(DIVIDENDS_APY)
            .setPriceMinYtd(PRICE_MIN_YTD)
            .setPriceMaxYtd(PRICE_MAX_YTD)
            .build();
}
