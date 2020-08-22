package com.dsi.adviser.integration.priceData;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("PRICE_DATA")
public class PriceDataEntity {

    @Id
    @Column("ID")
    Long id;

    @Column("STOCK_CODE")
    String stockCode;

    @Column("DATE")
    LocalDate date;

    @Column("PRICE_OPEN")
    Double priceOpen;

    @Column("PRICE_CLOSE")
    Double priceClose;

    @Column("PRICE_MIN")
    Double priceMin;

    @Column("PRICE_MAX")
    Double priceMax;

    @Column("VOLUME")
    Double volume;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
