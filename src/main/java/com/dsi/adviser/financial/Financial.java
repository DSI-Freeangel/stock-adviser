package com.dsi.adviser.financial;

import lombok.*;

import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Financial {
    String stockCodeFull;
    Integer year;
    Double enterpriseValue;
    Double earnings;
    Double dividendsApy;
    Double priceMinYtd;
    Double priceMaxYtd;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}
