package com.dsi.adviser.integration.client;

import com.dsi.adviser.integration.financialData.Source;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FinancialDataModel implements FinancialDataItem {
    String stockCode;
    LocalDate date;
    String jsonResponse;
    Source source;
}