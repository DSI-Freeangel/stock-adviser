package com.dsi.adviser.integration.client.alphavantage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = PriceItem.PriceItemBuilder.class)
public class PriceItem {
    @JsonProperty("1. open")
    Double priceOpen;
    @JsonProperty("4. close")
    Double priceClose;
    @JsonProperty("3. low")
    Double priceMin;
    @JsonProperty("2. high")
    Double priceMax;
    @JsonProperty("5. volume")
    Double volume;
}