package com.dsi.adviser.financial;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface FinancialData {
    String getStockCode();
    LocalDate getDate();
    Double getMarketCapitalization();
    Double getEnterpriseValue();
    Double getEarnings();
    Double getDividendsApy();
    Double getPriceMinYtd();
    Double getPriceMaxYtd();
    LocalDateTime getCreatedDate();
    LocalDateTime getUpdatedDate();
}