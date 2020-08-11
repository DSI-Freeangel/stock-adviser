package com.dsi.adviser.integration.client.alphavantage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Map;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = PriceSeries.PriceSeriesBuilder.class)
public class PriceSeries {
    @JsonProperty("Time Series (Daily)")
    Map<LocalDate, PriceItem> prices;
}