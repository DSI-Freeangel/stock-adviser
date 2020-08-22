package com.dsi.adviser.stock;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockModel implements Stock {
    String stockCodeFull;
    String stockCode;
    String exchange;
    String name;
    String sector;
    String industry;
}
