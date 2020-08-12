package com.dsi.adviser.financial;

import java.time.LocalDate;

public interface FinancialData {
    String getStockCodeFull();

    LocalDate getDate();

    Double getEnterpriseValue();

    Double getEarnings();

    Double getDividendsApy();

    Double getPriceMinYtd();

    Double getPriceMaxYtd();

    java.time.LocalDateTime getCreatedDate();

    java.time.LocalDateTime getUpdatedDate();
}