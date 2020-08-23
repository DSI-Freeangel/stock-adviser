package com.dsi.adviser.financial;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("FINANCIAL")
public class FinancialEntity {
    @Id
    @Column("ID")
    Long id;

    @Column("STOCK_CODE")
    String stockCode;

    @Column("DATE")
    LocalDate date;

    @Column("MARKET_CAPITALIZATION")
    Double marketCapitalization;

    @Column("ENTERPRISE_VALUE")
    Double enterpriseValue;

    @Column("EARNINGS")
    Double earnings;

    @Column("DIVIDENDS_APY")
    Double dividendsApy;

    @Column("PRICE_MIN_YTD")
    Double priceMinYtd;

    @Column("PRICE_MAX_YTD")
    Double priceMaxYtd;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
