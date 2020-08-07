package com.dsi.adviser.financial;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "FINANCIAL")
public class FinancialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STOCK_CODE_FULL")
    private String stockCodeFull;

    @Column(name = "YEAR")
    private Integer year;

    @Column(name = "ENTERPRISE_VALUE")
    private Double enterpriseValue;

    @Column(name = "EARNINGS")
    private Double earnings;

    @Column(name = "DIVIDENDS_APY")
    private Double dividendsApy;

    @Column(name = "PRICE_MIN_YTD")
    private Double priceMinYtd;

    @Column(name = "PRICE_MAX_YTD")
    private Double priceMaxYtd;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
