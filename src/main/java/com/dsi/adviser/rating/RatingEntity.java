package com.dsi.adviser.rating;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("RATING")
public class RatingEntity {

    @Id
    @Column("ID")
    Long id;

    @Column("STOCK_CODE")
    String stockCode;

    @Column("DATE")
    LocalDate date;

    @Column("BEAUTY")
    Double beauty;

    @Column("APY_GROWN")
    Double apyGrown;

    @Column("DISCOUNT")
    Double discount;

    @Column("EARNING_VALUE")
    Double earningValue;

    @Column("HYPERBOLIC")
    Double hyperbolic;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
