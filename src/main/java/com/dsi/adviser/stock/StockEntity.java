package com.dsi.adviser.stock;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("STOCK")
public class StockEntity {

    @Id
    @Column("ID")
    Long id;

    @Column("CODE_FULL")
    String stockCodeFull;

    @Column("CODE")
    String stockCode;

    @Column("EXCHANGE")
    String exchange;

    @Column("NAME")
    String name;

    @Column("SECTOR")
    String sector;

    @Column("INDUSTRY")
    String industry;

    @Column("ACTIVE")
    Boolean active;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
