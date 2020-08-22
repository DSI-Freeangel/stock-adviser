package com.dsi.adviser.integration.client;

import com.dsi.adviser.integration.financialData.Source;

import java.time.LocalDate;

public interface FinancialDataItem {
    String getStockCode();
    LocalDate getDate();
    String getJsonResponse();
    Source getSource();
}