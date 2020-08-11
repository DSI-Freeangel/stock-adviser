package com.dsi.adviser.stock;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("STOCK")
public class StockEntity {

    @Id
    @Column("ID")
    Long id;

    @Column("CODE_FULL")
    String codeFull;

    @Column("CODE")
    String code;

    @Column("EXCHANGE")
    String exchange;

    @Column("SECTOR")
    String sector;

    @Column("INDUSTRY")
    String industry;

    @Column("DATA_UPDATED_DATE")
    LocalDate dataUpdatedDate;

    @Column("ACTIVE")
    Boolean active = true;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
