package com.dsi.adviser.price;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PriceModel implements PriceData {
    String stockCodeFull;
    Period type;
    LocalDate date;
    Double price;
    Double priceMin;
    Double priceMax;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}