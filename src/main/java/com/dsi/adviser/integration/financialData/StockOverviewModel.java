package com.dsi.adviser.integration.financialData;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockOverviewModel implements StockOverviewData {
    String stockCode;
    LocalDate date;
    String name;
    String exchange;
    String sector;
    String industry;
    Double enterpriseValue;
    Double earnings;
    Double dividendsApy;
    Double priceMinYtd;
    Double priceMaxYtd;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}