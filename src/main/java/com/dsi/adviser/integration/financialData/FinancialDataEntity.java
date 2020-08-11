package com.dsi.adviser.integration.financialData;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(setterPrefix = "set", toBuilder = true)
@Table("FINANCIAL_DATA")
public class FinancialDataEntity {

    @Id
    @Column("ID")
    Long id;

    @Column("STOCK_CODE_FULL")
    String stockCodeFull;

    @Column("DATE")
    LocalDate date;

    @Column("SOURCE")
    Source source;

    @Column("JSON_RESPONSE")
    String jsonResponse;

    @Column("CREATED_DATE")
    LocalDateTime createdDate;

    @Column("UPDATED_DATE")
    LocalDateTime updatedDate;
}
