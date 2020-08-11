package com.dsi.adviser.financial;

public interface FinancialData {
    String getStockCodeFull();

    Integer getYear();

    Double getEnterpriseValue();

    Double getEarnings();

    Double getDividendsApy();

    Double getPriceMinYtd();

    Double getPriceMaxYtd();

    java.time.LocalDateTime getCreatedDate();

    java.time.LocalDateTime getUpdatedDate();
}