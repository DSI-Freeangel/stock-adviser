package com.dsi.adviser.integration.priceData;

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
@Table(name = "PRICE_DATA")
public class PriceDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STOCK_CODE_FULL")
    private String stockCodeFull;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "PRICE_OPEN")
    private Double priceOpen;

    @Column(name = "PRICE_CLOSE")
    private Double priceClose;

    @Column(name = "PRICE_MIN")
    private Double priceMin;

    @Column(name = "PRICE_MAX")
    private Double priceMax;

    @Column(name = "VOLUME")
    private Double volume;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
