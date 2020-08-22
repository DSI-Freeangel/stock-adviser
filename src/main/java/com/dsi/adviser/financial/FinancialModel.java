package com.dsi.adviser.financial;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FinancialModel implements FinancialData {
    String stockCode;
    LocalDate date;
    Double enterpriseValue;
    Double earnings;
    Double dividendsApy;
    Double priceMinYtd;
    Double priceMaxYtd;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}
