package com.dsi.adviser.price;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("PRICE")
public class PriceEntity {
    @Id
    @Column("ID")
    Long id;

    @Column("STOCK_CODE_FULL")
    String stockCodeFull;

    @Column("TYPE")
    Period type;

    @Column("DATE")
    LocalDate date;

    @Column("PRICE")
    Double price;

    @Column("PRICE_MIN")
    Double priceMin;

    @Column("PRICE_MAX")
    Double priceMax;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
