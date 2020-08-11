package com.dsi.adviser.financial;

class TestData {
    static final String STOCK_CODE_FULL = "IBM";
    static final int YEAR = 2020;
    static final double ENTERPRISE_VALUE = 20.0;
    static final double EARNINGS = 1.0;
    static final double DIVIDENDS_APY = 1.0;
    static final double PRICE_MIN_YTD = 2.0;
    static final double PRICE_MAX_YTD = 3.0;

    static final FinancialEntity FINANCIAL_ENTITY = FinancialEntity.builder()
            .setStockCodeFull(STOCK_CODE_FULL)
            .setYear(YEAR)
            .setEnterpriseValue(ENTERPRISE_VALUE)
            .setEarnings(EARNINGS)
            .setDividendsApy(DIVIDENDS_APY)
            .setPriceMinYtd(PRICE_MIN_YTD)
            .setPriceMaxYtd(PRICE_MAX_YTD)
            .build();

    static final Financial FINANCIAL_MODEL = Financial.builder()
            .setStockCodeFull(STOCK_CODE_FULL)
            .setYear(YEAR)
            .setEnterpriseValue(ENTERPRISE_VALUE)
            .setEarnings(EARNINGS)
            .setDividendsApy(DIVIDENDS_APY)
            .setPriceMinYtd(PRICE_MIN_YTD)
            .setPriceMaxYtd(PRICE_MAX_YTD)
            .build();
}
