package com.dsi.adviser.rating;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("PRICE")
public class RatingEntity {

    @Id
    @Column("ID")
    Long id;

    @Column("STOCK_CODE_FULL")
    String stockCodeFull;

    @Column("BEAUTY")
    Double beauty;

    @Column("APY_GROWN")
    Double apyGrown;

    @Column("EARNING_VALUE")
    Double earningValue;

    @Column("HYPERBOLIC")
    Double hyperbolic;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
