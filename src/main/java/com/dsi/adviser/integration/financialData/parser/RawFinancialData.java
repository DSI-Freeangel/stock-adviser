package com.dsi.adviser.integration.financialData.parser;

import java.time.LocalDate;

public interface RawFinancialData {
    LocalDate getDate();

    Double getMarketCapitalization();

    Double getEnterpriseValue();

    Double getEarnings();

    Double getDividendsApy();

    Double getPriceMinYtd();

    Double getPriceMaxYtd();

    String getName();

    String getExchange();

    String getSector();

    String getIndustry();
}