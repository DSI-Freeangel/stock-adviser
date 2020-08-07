package com.dsi.adviser.integration.financialData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "FINANCIAL_DATA")
public class FinancialDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STOCK_CODE_FULL")
    private String stockCodeFull;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "JSON_RESPONSE")
    private String jsonResponse;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
