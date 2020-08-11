package com.dsi.adviser.integration.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PriceDataModel implements PriceDataItem {
    LocalDate date;
    Double priceOpen;
    Double priceClose;
    Double priceMin;
    Double priceMax;
    Double volume;
}