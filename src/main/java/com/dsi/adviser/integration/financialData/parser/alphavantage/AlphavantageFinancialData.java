package com.dsi.adviser.integration.financialData.parser.alphavantage;

import com.dsi.adviser.integration.financialData.parser.RawFinancialData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = AlphavantageFinancialData.AlphavantageFinancialDataBuilder.class)
public class AlphavantageFinancialData implements RawFinancialData {
    @JsonProperty("LatestQuarter")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    LocalDate date;
    @JsonProperty("EVToEBITDA")
    @JsonDeserialize(using = DoubleDeserializer.class)
    Double enterpriseValue;
    @JsonProperty("EBITDA")
    @JsonDeserialize(using = DoubleDeserializer.class)
    Double earnings;
    @JsonProperty("MarketCapitalization")
    @JsonDeserialize(using = DoubleDeserializer.class)
    Double marketCapitalization;
    @JsonProperty("DividendYield")
    @JsonDeserialize(using = DoubleDeserializer.class)
    Double dividendsApy;
    @JsonProperty("52WeekLow")
    @JsonDeserialize(using = DoubleDeserializer.class)
    Double priceMinYtd;
    @JsonProperty("52WeekHigh")
    @JsonDeserialize(using = DoubleDeserializer.class)
    Double priceMaxYtd;
    @JsonProperty("Name")
    String name;
    @JsonProperty("Exchange")
    String exchange;
    @JsonProperty("Sector")
    String sector;
    @JsonProperty("Industry")
    String industry;
}